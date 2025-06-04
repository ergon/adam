package ch.ergon.adam.mariadb;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.jooq.JooqSource;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class MariaDbSource extends JooqSource {
    private final String schemaName;
    private Map<String, DbEnum> enums;

    public MariaDbSource(String url, String schemaName) throws SQLException {
        super(url, schemaName);
        this.schemaName = schemaName;
        this.setSqlDialect(SQLDialect.MARIADB);
    }

    public MariaDbSource(Connection connection, String schemaName) {
        super(connection, schemaName);
        this.setSqlDialect(SQLDialect.MARIADB);
        this.schemaName = schemaName;
    }

    @Override
    public Schema getSchema() {
        enums = getEnums().stream().collect(toLinkedMap(DbEnum::getName, identity()));
        Schema schema = super.getSchema();
        schema.setSequences(getSequences());
        schema.setEnums(enums.values());
        setCustomTypes(schema);
        fetchConstraints(schema);
        return schema;
    }

    @Override
    protected boolean filterTable(org.jooq.Table<?> table) {
        // Sequences are generated as tables in MariaDB and thus should be filtered
        // https://github.com/jOOQ/jOOQ/issues/9291
        return getSequencesName().stream().noneMatch(s -> s.equals(table.getName()));
    }

    @Override
    protected Map<String, List<String>> fetchViewDependencies() {
        Result<Record> result = getContext().resultQuery(
            "SELECT v.TABLE_NAME AS view, TRIM(t.TABLE_NAME) AS base " +
                "FROM INFORMATION_SCHEMA.VIEWS v " +
                "JOIN INFORMATION_SCHEMA.TABLES t ON t.TABLE_SCHEMA = v.TABLE_SCHEMA " +
                "WHERE v.TABLE_SCHEMA = ? " +
                "AND v.TABLE_NAME != t.TABLE_NAME " +
                "AND LOCATE(CONCAT('`', t.TABLE_NAME, '`'), v.VIEW_DEFINITION) > 0 " +
                "GROUP BY v.TABLE_NAME, t.TABLE_NAME", schemaName).fetch();

        return result.stream()
            .collect(
                groupingBy(r -> r.getValue("view").toString(),
                    mapping(r -> r.getValue("base").toString(), toList())));
    }

    @Override
    protected DataType mapDataTypeFromJooq(org.jooq.Field<?> jooqField, org.jooq.Table<?> jooqTable) {
        String enumName = jooqTable.getName() + "_" +  jooqField.getName();
        if (enums.containsKey(enumName)) {
            return ENUM;
        }
        return super.mapDataTypeFromJooq(jooqField, jooqTable);
    }

    @Override
    protected String getViewDefinition(String name) {
        Result<Record> result = getContext().resultQuery("select view_definition from INFORMATION_SCHEMA.views where table_schema = ? and table_name = ?", schemaName, name).fetch();
        String definition = result.getFirst().getValue("view_definition").toString();
        return definition.replaceAll("`" + schemaName + "`\\.", "");
    }

    private Collection<Sequence> getSequences() {
        List<Sequence> result = new ArrayList<>();
        for (String seqName : getSequencesName()) {
            Result<Record> seqDetails = getContext().resultQuery("SHOW CREATE SEQUENCE " + seqName).fetch();

            if (!seqDetails.isEmpty()) {
                result.add(
                    parseSequenceFromCreateStatement(
                        seqName,
                        seqDetails.getValue(0, "Create Table").toString().toUpperCase()
                    )
                );
            }
        }

        return result.stream()
            .sorted(comparing(Sequence::getName))
            .collect(toList());
    }

    private List<String> getSequencesName() {
        Result<Record> sequences = getContext().resultQuery(
            "SELECT TABLE_NAME as sequence_name " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = ? " +
                "AND TABLE_TYPE = 'SEQUENCE'",
            schemaName).fetch();
        return sequences.stream().map(r -> r.getValue("sequence_name").toString()).collect(toList());
    }

    private Sequence parseSequenceFromCreateStatement(String name, String sequenceStatement) {
        Sequence sequence = new Sequence(name);

        Pattern startPattern = Pattern.compile("START\\s+WITH\\s+(\\d+)");
        Pattern minPattern = Pattern.compile("MINVALUE\\s+(\\d+)");
        Pattern maxPattern = Pattern.compile("MAXVALUE\\s+(\\d+)");
        Pattern incrementPattern = Pattern.compile("INCREMENT\\s+BY\\s+(\\d+)");

        Matcher matcher = startPattern.matcher(sequenceStatement);
        if (matcher.find()) {
            sequence.setStartValue(Long.valueOf(matcher.group(1)));
        }
        matcher = minPattern.matcher(sequenceStatement);
        if (matcher.find()) {
            sequence.setMinValue(Long.valueOf(matcher.group(1)));
        }
        matcher = maxPattern.matcher(sequenceStatement);
        if (matcher.find()) {
            sequence.setMaxValue(Long.valueOf(matcher.group(1)));
        }
        matcher = incrementPattern.matcher(sequenceStatement);
        if (matcher.find()) {
            sequence.setIncrement(Long.valueOf(matcher.group(1)));
        }
        return sequence;
    }

    private void fetchConstraints(Schema schema) {
        Result<Record> result = getContext().resultQuery(
            "SELECT tc.TABLE_NAME as relname, " +
                "tc.CONSTRAINT_NAME as conname, " +
                "tc.CONSTRAINT_TYPE as contype, " +
                "cc.CHECK_CLAUSE as expression " +
                "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc " +
                "LEFT JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc " +
                "ON tc.CONSTRAINT_SCHEMA = cc.CONSTRAINT_SCHEMA " +
                "AND tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME " +
                "WHERE tc.CONSTRAINT_TYPE IN ('PRIMARY KEY', 'CHECK') " +
                "AND tc.TABLE_SCHEMA = ?",
            schemaName).fetch();

        Map<Table, List<Record>> byTable = result.stream().collect(groupingBy(
            record -> schema.getTable(record.getValue("relname").toString()),
            toList()));

        byTable.keySet().forEach(table ->
            table.setConstraints(byTable.get(table).stream()
                .map(this::mapConstraintFromMariaDB)
                .collect(toList())));
    }

    private Constraint mapConstraintFromMariaDB(Record record) {
        String constraintType = record.getValue("contype").toString();
        String name = record.getValue("conname").toString();

        switch (constraintType) {
            case "PRIMARY KEY":
                return new PrimaryKeyConstraint(name);
            case "CHECK":
                RuleConstraint ruleConstraint = new RuleConstraint(name);
                String expression = record.getValue("expression").toString();
                ruleConstraint.setRule(expression);
                return ruleConstraint;
            default:
                throw new RuntimeException(format("Unsupported constraint type [%s]", constraintType));
        }
    }

    private Collection<DbEnum> getEnums() {
        Result<Record> result = getEnumSchemaInformation();

        return result.stream()
            .map(this::mapEnumFromMariaDB)
            .sorted(comparing(DbEnum::getName))
            .collect(toList());
    }

    private Result<Record> getEnumSchemaInformation() {
        return getContext().resultQuery(
            "SELECT DISTINCT" +
                "    TABLE_NAME AS table_name," +
                "    COLUMN_NAME AS column_name," +
                "    CONCAT(TABLE_NAME, '_', COLUMN_NAME) AS enum_name," +
                "    GROUP_CONCAT(" +
                "        REPLACE(REPLACE(SUBSTRING(COLUMN_TYPE, 6, LENGTH(COLUMN_TYPE) - 6), '''', ''), ' ', '')" +
                "        ORDER BY ORDINAL_POSITION" +
                "    ) AS enum_values " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? " +
                "AND COLUMN_TYPE LIKE 'enum%' " +
                "GROUP BY TABLE_NAME, COLUMN_NAME",
            schemaName).fetch();
    }

    private DbEnum mapEnumFromMariaDB(Record record) {
        DbEnum dbEnum = new DbEnum(record.getValue("enum_name").toString());
        dbEnum.setValues(record.getValue("enum_values").toString().split(","));
        return dbEnum;
    }

    private void setCustomTypes(Schema schema) {
        getEnumSchemaInformation().forEach(record -> {
            Relation relation = schema.getRelation(record.getValue("table_name").toString());
            if (relation == null) {
                return;
            }
            Field field = relation.getField(record.getValue("column_name").toString());
            String enumName = record.getValue("enum_name").toString();
            DbEnum dbEnum = requireNonNull(schema.getEnum(enumName), "Unknown enum [" + enumName + "] for field [" + field.getName() + "] on table [" + relation.getName() + "].");
            field.setDataType(ENUM);
            field.setDbEnum(dbEnum);
        });
    }
}

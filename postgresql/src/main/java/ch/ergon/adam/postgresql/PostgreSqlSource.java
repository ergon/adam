package ch.ergon.adam.postgresql;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.jooq.JooqSource;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class PostgreSqlSource extends JooqSource {

    private static final Pattern CHECK_CONSTRAINT_PATTERN = Pattern.compile("^CHECK \\((.*)\\)$");
    private static final Pattern DEFAULT_CAST_PATTERN = Pattern.compile("cast\\((.*) as [^ ^)]+( array)?\\)");


    private final String schemaName;
    private Map<String, DbEnum> enums;

    public PostgreSqlSource(String url, String schemaName) throws SQLException {
        super(url, schemaName);
        this.schemaName = schemaName;
        this.setSqlDialect(SQLDialect.POSTGRES);
    }

    public PostgreSqlSource(Connection connection, String schemaName) {
        super(connection, schemaName);
        this.setSqlDialect(SQLDialect.POSTGRES);
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
    protected String getDefaultValue(org.jooq.Field<?> jooqField) {
        String defaultValue = super.getDefaultValue(jooqField);
        if (defaultValue == null) {
            return null;
        }
        Matcher matcher = DEFAULT_CAST_PATTERN.matcher(defaultValue);
        defaultValue = matcher.replaceAll(r -> r.group(1) );
        return defaultValue;
    }

    @Override
    protected Map<String, List<String>> fetchViewDependencies() {
        Result<Record> result = getContext().resultQuery(
            "SELECT cl_r.relname AS view, cl_d.relname AS base " +
                "FROM pg_rewrite AS r " +
                "JOIN pg_class AS cl_r ON r.ev_class=cl_r.oid " +
                "JOIN pg_depend AS d ON r.oid = d.objid " +
                "JOIN pg_class AS cl_d ON d.refobjid = cl_d.oid " +
                "JOIN pg_namespace AS ns ON cl_d.relnamespace = ns.oid AND ns.oid = cl_r.relnamespace " +
                "WHERE cl_d.relkind IN ('v', 'r') and cl_r.relname != cl_d.relname AND ns.nspname = ? " +
                "GROUP BY cl_r.relname, cl_d.relname, cl_d.relkind", schemaName).fetch();

        return result.stream()
            .collect(
                groupingBy(r -> r.getValue("view").toString(),
                mapping(r -> r.getValue("base").toString(), toList())));
    }

    private void setCustomTypes(Schema schema) {
        Result<Record> result = getContext().resultQuery("SELECT table_name, column_name, udt_name, is_nullable, data_type FROM information_schema.columns WHERE udt_name IS NOT NULL AND udt_schema = ?", schemaName).fetch();
        for (Record record: result) {
            Relation relation = schema.getRelation(record.getValue("table_name").toString());
            if (relation == null) {
                continue;
            }
            Field field = relation.getField(record.getValue("column_name").toString());
            String udtName = record.getValue("udt_name").toString();
            if ("ARRAY".equals(record.getValue("data_type"))) {
                field.setArray(true);
                if (udtName.startsWith("_")) {
                    udtName = udtName.substring(1);
                }
            }
            DbEnum dbEnum = requireNonNull(schema.getEnum(udtName), "Unknown udt name [" + udtName + "] for field [" + field.getName() + "] on table [" + relation.getName() + "].");
            boolean isNullable = record.getValue("is_nullable").toString().toLowerCase().equals("yes");
            field.setDataType(ENUM);
            field.setDbEnum(dbEnum);
            field.setNullable(isNullable);
        }
    }

    private Collection<DbEnum> getEnums() {
        Result<Record> result = getContext().resultQuery("SELECT t.typname, string_agg(e.enumlabel, '|' ORDER BY e.enumsortorder) AS enum_labels " +
            "FROM pg_catalog.pg_type t " +
            "JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace " +
            "JOIN pg_catalog.pg_enum e ON t.oid = e.enumtypid " +
            "WHERE n.nspname = ? " +
            "GROUP BY typname;", schemaName).fetch();
        return result.stream().map(this::mapEnumFromPostgres).sorted(comparing(DbEnum::getName)).collect(toList());

    }

    private Collection<Sequence> getSequences() {
        Result<Record> result = getContext().resultQuery("SELECT sequence_name, start_value, minimum_value, maximum_value, increment " +
                "FROM information_schema.sequences where sequence_schema = ? and sequence_name in ( " +
                "select seq.relname " +
                "from pg_class as seq " +
                "JOIN pg_namespace AS ns ON seq.relnamespace = ns.oid " +
                "where ns.nspname = ? and seq.relkind='S' " +
                "and seq.relfilenode not in (select dep.objid from pg_depend dep join pg_class as tab on (dep.refobjid = tab.oid)))",
            schemaName, schemaName).fetch();
        return result.stream().map(this::mapSequenceFromPostgres).sorted(comparing(Sequence::getName)).collect(toList());
    }

    private Sequence mapSequenceFromPostgres(Record record) {
        Sequence sequence = new Sequence(record.getValue("sequence_name").toString());
        sequence.setStartValue(Long.valueOf(record.getValue("start_value").toString()));
        sequence.setMinValue(Long.valueOf(record.getValue("minimum_value").toString()));
        sequence.setMaxValue(Long.valueOf(record.getValue("maximum_value").toString()));
        sequence.setIncrement(Long.valueOf(record.getValue("increment").toString()));
        return sequence;
    }

    private void fetchConstraints(Schema schema) {
        Result<Record> result = getContext().resultQuery("SELECT rel.relname, con.conname, con.contype, pg_get_constraintdef(con.oid) AS expression " +
            "FROM pg_catalog.pg_constraint con " +
            "INNER JOIN pg_catalog.pg_class rel ON rel.oid = con.conrelid " +
            "INNER JOIN pg_catalog.pg_namespace nsp ON nsp.oid = connamespace " +
            "WHERE con.contype IN ('c', 'p') AND nsp.nspname = ?", schemaName).fetch();

        Map<Table, List<Record>> byTable = result.stream().collect(groupingBy(
            record -> schema.getTable(record.getValue("relname").toString()),
            toList()));

        byTable.keySet().forEach(table ->
            table.setConstraints(byTable.get(table).stream()
                .map(this::mapConstraintFromPostgres)
                .collect(toList())));
    }

    private Constraint mapConstraintFromPostgres(Record record) {
        String constraintType = record.getValue("contype").toString();
        String name = record.getValue("conname").toString();
        switch (constraintType) {
            case "p":
                return new PrimaryKeyConstraint(name);
            case "c":
                RuleConstraint ruleConstraint = new RuleConstraint(name);
                String expression = record.getValue("expression").toString();
                Matcher matcher = CHECK_CONSTRAINT_PATTERN.matcher(expression);
                if (!matcher.find()) {
                    throw new RuntimeException(format("Rule [%s] for constraint [%s] could not be parsed.", expression, name));
                }
                ruleConstraint.setRule(matcher.group(1));
                return ruleConstraint;
            default:
                throw new RuntimeException(format("Unsupported constraint type [%s]", constraintType));
        }
    }

    private DbEnum mapEnumFromPostgres(Record record) {
        DbEnum dbEnum = new DbEnum(record.getValue(0).toString());
        dbEnum.setValues(record.getValue(1).toString().split("\\|"));
        return dbEnum;
    }

    @Override
    protected String getViewDefinition(String name) {
        Result<Record> result = getContext().resultQuery("select view_definition from INFORMATION_SCHEMA.views where table_schema = ? and table_name = ?", schemaName, name).fetch();
        return result.getFirst().getValue("view_definition").toString();
    }

    @Override
    protected DataType mapDataTypeFromJooq(org.jooq.Field<?> jooqField) {
        String typeName = jooqField.getDataType().isArray() ? jooqField.getDataType().getArrayComponentDataType().getTypeName() : jooqField.getDataType().getTypeName();
        switch (typeName) {
            case "other":
                return null; // Will be set later
            case "text":
                // Only if type is array of text
                return DataType.CLOB;
        }
        if (enums.containsKey(typeName) || enums.containsKey(typeName.replaceFirst("_", ""))) {
            return ENUM;
        }
        return super.mapDataTypeFromJooq(jooqField);
    }
}

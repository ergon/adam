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

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class PostgreSqlSource extends JooqSource {

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
        schema.setViews(getViews());
        schema.getViews().forEach(view -> view.setFields(schema.getTable(view.getName()).getFields()));
        schema.getTables().removeIf(table -> schema.getView(table.getName()) != null);
        fetchDefaults(schema);
        setCustomTypes(schema);
        fetchConstraints(schema);
        fetchViewDependencies(schema);
        return schema;
    }

    private void fetchDefaults(Schema schema) {
        //TODO: Remove as soon as https://github.com/jOOQ/jOOQ/issues/8875 is fixed

        Result<Record> result = getContext().resultQuery(
            "SELECT column_name, column_default, table_name\n" +
                "FROM information_schema.columns where table_schema = ? and column_default is not null", schemaName).fetch();

        for (Record record : result) {
            String tableName = record.getValue("table_name").toString();
            String columnName = record.getValue("column_name").toString();
            String defaultValue = record.getValue("column_default").toString();

            Table table = schema.getTable(tableName);
            if (table == null) {
                continue;
            }

            Field field = table.getField(columnName);
            if (field == null || field.isSequence() || (field.getDefaultValue() != null && field.getDefaultValue() != "null")) {
                continue;
            }

            field.setDefaultValue(defaultValue);
        }

        for (Table table : schema.getTables()) {
            for (Field field : table.getFields()) {
                String defaultValue = field.getDefaultValue();
                if (defaultValue == null) {
                    continue;
                }

                if (field.isArray() && defaultValue.endsWith("[]")) {
                    defaultValue = defaultValue.substring(0, defaultValue.length() - 2);
                }

                field.setDefaultValue(defaultValue.replaceAll("::[a-z A-Z_]*", ""));
            }

        }

    }

    @Override
    protected String getDefaultValue(org.jooq.Field<?> jooqField) {
        String defaultValue = super.getDefaultValue(jooqField);
        if (defaultValue == null) {
            return null;
        }
        if (jooqField.getDataType().isArray() && defaultValue.endsWith("[]")) {
            defaultValue = defaultValue.substring(0, defaultValue.length() - 2);
        }
        return defaultValue.replaceAll("::[a-z A-Z_]*", "");
    }

    private void fetchViewDependencies(Schema schema) {
        Result<Record> result = getContext().resultQuery(
            "SELECT cl_r.relname AS view, cl_d.relname AS base, cl_d.relkind basetype " +
                "FROM pg_rewrite AS r " +
                "JOIN pg_class AS cl_r ON r.ev_class=cl_r.oid " +
                "JOIN pg_depend AS d ON r.oid = d.objid " +
                "JOIN pg_class AS cl_d ON d.refobjid = cl_d.oid " +
                "JOIN pg_namespace AS ns ON cl_d.relnamespace = ns.oid AND ns.oid = cl_r.relnamespace " +
                "WHERE cl_d.relkind IN ('v', 'r') and cl_r.relname != cl_d.relname AND ns.nspname = ? " +
                "GROUP BY cl_r.relname, cl_d.relname, cl_d.relkind", schemaName).fetch();

        for (Record record : result) {
            String viewName = record.getValue("view").toString();
            String baseName = record.getValue("base").toString();
            String baseType = record.getValue("basetype").toString();

            View view = schema.getView(viewName);
            Relation relation = "v".equals(baseType) ? schema.getView(baseName) : schema.getTable(baseName);
            view.addBaseRelation(relation);
        }
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
        Result<Record> result = getContext().resultQuery("SELECT rel.relname, con.conname, con.contype, con.consrc " +
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
                ruleConstraint.setRule(record.getValue("consrc").toString());
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

    private Collection<View> getViews() {
        Result<Record> result = getContext().resultQuery("select table_name, view_definition from INFORMATION_SCHEMA.views where table_schema = ?", schemaName).fetch();
        return result.stream().map(this::mapViewFromJooq).sorted(comparing(View::getName)).collect(toList());
    }

    private View mapViewFromJooq(Record record) {
        String viewName = record.getValue("table_name").toString();
        String viewDefinition = record.getValue("view_definition").toString();
        View view = new View(viewName);
        view.setViewDefinition(viewDefinition);
        return view;
    }

    @Override
    protected DataType mapDataTypeFromJooq(org.jooq.Field<?> jooqField) {
        String typeName = jooqField.getDataType().getTypeName();
        switch (typeName) {
            case "other":
                return null; // Will be set later
            case "text":
                // Only if type is array of text
                return DataType.CLOB;
        }
        if (enums.containsKey(typeName)) {
            return ENUM;
        }
        return super.mapDataTypeFromJooq(jooqField);
    }
}

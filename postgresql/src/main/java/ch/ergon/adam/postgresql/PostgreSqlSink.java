package ch.ergon.adam.postgresql;

import ch.ergon.adam.jooq.JooqSink;
import ch.ergon.adam.core.db.schema.DbEnum;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Sequence;
import ch.ergon.adam.core.db.schema.Table;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;

import java.sql.Connection;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.createQuotedList;
import static java.lang.String.format;
import static org.jooq.SQLDialect.POSTGRES;
import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

public class PostgreSqlSink extends JooqSink {

    private String schemaName;

    public PostgreSqlSink(Connection dbConnection, String schema) {
        super(dbConnection, POSTGRES, schema);
        this.schemaName = schema;
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        context.execute(format("DROP TYPE \"%s\"", dbEnum.getName()));
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        context.execute(format("CREATE TYPE \"%s\" AS ENUM (%s)", dbEnum.getName(), createQuotedList(dbEnum.getValues(), "'")));
    }

    @Override
    public void changeFieldType(Field oldField, Field newField, ch.ergon.adam.core.db.schema.DataType targetDataType) {
        String using ="";
        String newTypeName;
        if (targetDataType == ch.ergon.adam.core.db.schema.DataType.ENUM) {
            using = String.format(" USING %s::%s", newField.getName(), newField.getDbEnum().getName());
            newTypeName = mapType(newField).getTypeName();
            if (newField.isArray()) {
                using += "[]";
            }
        } else {
            newTypeName = mapRawType(targetDataType).getTypeName();
        }
        context.execute(format("ALTER TABLE \"%s\" ALTER COLUMN \"%s\" TYPE %s%s", oldField.getTable().getName(), oldField.getName(), newTypeName, using));
    }

    @Override
    protected DataType<?> mapType(Field field) {
        if (field.isSequence()) {
            if (field.getDataType() == ch.ergon.adam.core.db.schema.DataType.BIGINT) {
                return new DefaultDataType<>(null, BIGINT, "bigserial", "bigserial")
                    .nullable(false);
            } else {
                return new DefaultDataType<>(null, INTEGER, "serial", "serial")
                    .nullable(false);
            }
        }
        return super.mapType(field);
    }

    @Override
    protected DataType<?> mapFieldToJooqType(Field field) {
        switch (field.getDataType()) {
            case ENUM:
                return new DefaultDataType<>(null, VARCHAR, field.getDbEnum().getName(), field.getDbEnum().getName());
            default:
                return super.mapFieldToJooqType(field);
        }
    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {
        super.copyData(sourceTable, targetTable, sourceTableName);

        //Fix the sequence after data copy
        targetTable.getFields().stream().filter(Field::isSequence).forEach(sequenceField -> {
            String sequenceName = context.fetch(format("select pg_get_serial_sequence('%s','%s')", targetTable.getName(), sequenceField.getName())).getValue(0, 0).toString();
            context.execute(format(
                "BEGIN;\n" +
                    "-- protect against concurrent inserts while you update the counter\n" +
                    "LOCK TABLE \"%s\" IN EXCLUSIVE MODE;\n" +
                    "-- Update the sequence\n" +
                    "SELECT setval('%s',(SELECT GREATEST(MAX(\"%s\")+1,nextval('%s'))-1 FROM \"%s\")) where exists (select 1 from \"%s\");\n" +
                    "COMMIT;", targetTable.getName(), sequenceName, sequenceField.getName(), sequenceName, targetTable.getName(), targetTable.getName()));
        });
    }

    @Override
    protected String castIfNeeded(Field sourceField, Field targetField, DSLContext renderContext) {
        if (targetField.getDataType() == ENUM && sourceField.getDataType() == ENUM && !sourceField.getDbEnum().getName().equals(targetField.getDbEnum().getName())) {
            // Map enum to enum through VARCHAR
            DataType<?> targetType = mapType(targetField);
            org.jooq.Field<Object> jooqField = DSL.field("\"" + targetField.getName() + "\"");
            DataType<?> varCharType = new DefaultDataType<>(null, VARCHAR, "VARCHAR");
            return renderContext.render(DSL.cast(DSL.cast(jooqField, varCharType), targetType));
        }
        if (targetField.getDataType() != sourceField.getDataType()) {
            if (targetField.isSequence()) {
                DataType<?> targetType = mapType(targetField).getSQLDataType();
                org.jooq.Field<Object> jooqField = DSL.field("\"" + targetField.getName() + "\"");
                return format("cast(%s as %s)", jooqField.toString(), targetType.getCastTypeName());
            }
        }

        return super.castIfNeeded(sourceField, targetField, renderContext);
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {

        super.dropSequencesAndDefaults(table);

        Result<Record> result = context.resultQuery(
            "        SELECT\n" +
                "        seq_class.relname\n" +
                "            FROM\n" +
                "        pg_class seq_class\n" +
                "        join pg_depend depend on (seq_class.oid = depend.objid)\n" +
                "        join pg_class table_class on (table_class.oid = depend.refobjid)\n" +
                "        join pg_namespace ns on (table_class.relnamespace = ns.oid)\n" +
                "        WHERE\n" +
                "        seq_class.relkind = 'S' and\n" +
                "        table_class.relkind = 'r' and\n" +
                "        table_class.relname = ? and\n" +
                "        ns.nspname=?;", table.getName(), schemaName).fetch();

        result.forEach(row -> dropSequence(new Sequence(row.getValue(0, String.class))));
    }

    @Override
    public void dropSequence(Sequence sequence) {
        context.execute("drop sequence " + sequence.getName() + " cascade");
    }
}

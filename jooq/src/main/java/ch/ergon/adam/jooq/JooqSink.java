package ch.ergon.adam.jooq;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.Constraint;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.ForeignKey;
import ch.ergon.adam.core.db.schema.Index;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Sequence;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.core.db.schema.*;
import org.jooq.DataType;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE;

public class JooqSink implements SchemaSink {

    protected final DSLContext context;
    protected final String schema;

    public JooqSink(String url, String schema) {
        context = DSL.using(url);
        this.schema = schema;
    }

    public JooqSink(String url) {
        context = DSL.using(url);
        this.schema = null;
    }

    public JooqSink(Connection connection, SQLDialect dialect) {
        context = DSL.using(connection, dialect);
        this.schema = null;
    }

    protected JooqSink(Connection dbConnection, SQLDialect dialect, String schema) {
        context = DSL.using(dbConnection, dialect);
        if (!dialect.family().name().contains("ORACLE")) {
            context.createSchemaIfNotExists(schema).execute();
        }
        this.schema = schema;
    }

    @Override
    public void close() {
        if (context instanceof CloseableDSLContext) {
            ((CloseableDSLContext) context).close();
        }
    }


    protected Name getTableName(Table table) {
        return DSL.name(schema, table.getName());
    }

    protected Name getFieldName(Field field) {
        return DSL.name(field.getName());
    }

    @Override
    public void setTargetSchema(Schema targetSchema) {
        targetSchema.getEnums().forEach( e -> new DefaultDataType<>(null, Object.class, e.getName()));
    }

    @Override
    public void commitChanges() {
    }

    @Override
    public void rollback() {

    }

    @Override
    public void dropForeignKey(ForeignKey foreignKey) {
        context.alterTable(foreignKey.getTable().getName()).dropConstraint(foreignKey.getName()).execute();
    }

    @Override
    public void createForeignKey(ForeignKey foreignKey) {
        Index targetIndex = foreignKey.getTargetIndex();
        org.jooq.Constraint constraint = DSL.constraint(foreignKey.getName()).foreignKey(foreignKey.getField().getName())
            .references(targetIndex.getTable().getName(), targetIndex.getFields().getFirst().getName());
        context.alterTable(foreignKey.getTable().getName()).add(constraint).execute();
    }

    @Override
    public void dropIndex(Index index) {
        context.dropIndex(index.getName()).on(getTableName(index.getTable())).execute();

    }

    @Override
    public void createIndex(Index index) {
        if (index.isPrimary()) {
            ConstraintEnforcementStep primaryKey = DSL.constraint(index.getName()).primaryKey(createSchemaItemNameArray(index.getFields()));
            context.alterTable(index.getTable().getName()).add(primaryKey).execute();
        } else {
            Collection<Name> fieldNames = index.getFields().stream().map(this::getFieldName).collect(toList());
            CreateIndexStep createIndex;
            if (index.isUnique()) {
                createIndex = context.createUniqueIndex(index.getName());
            } else {
                createIndex = context.createIndex(index.getName());
            }
            CreateIndexIncludeStep indexStep = createIndex.on(getTableName(index.getTable()), fieldNames);
            if (index.getWhere() == null || index.getWhere().isEmpty()) {
                indexStep.execute();
            } else {
                indexStep.where(DSL.condition(index.getWhere())).execute();
            }
        }
    }

    @Override
    public void addField(Field field) {
        context.alterTable(getTableName(field.getTable())).addColumn(field.getName(), mapType(field)).execute();
        if (field.getSqlForNew() != null) {
            context.execute(String.format("UPDATE \"%s\" SET \"%s\" = %s", field.getTable().getName(), field.getName(), field.getSqlForNew()));
        }
    }

    @Override
    public void dropField(Field field, Table table) {
        context.alterTable(getTableName(table)).dropColumn(field.getName()).execute();
    }

    @Override
    public void dropDefault(Field field) {
        context.alterTable(getTableName(field.getTable())).alterColumn(field.getName()).defaultValue(DSL.field("null")).execute();
    }

    @Override
    public void setDefault(Field field) {
        if (field.getDefaultValue() == null) {
            dropDefault(field);
            return;
        }
        context.alterTable(getTableName(field.getTable())).alterColumn(field.getName()).defaultValue(getDefaultValue(field)).execute();
    }

    @Override
    public void createTable(Table table) {
        CreateTableElementListStep createTable = context.createTable(getTableName(table));
        CreateTableElementListStep addRows = null;
        for (Field field : table.getFields()) {
            if (addRows == null) {
                addRows = createTable.column(field.getName(), mapType(field));
            } else {
                addRows = addRows.column(field.getName(), mapType(field));
            }
        }
        if (addRows == null) {
            throw new RuntimeException("Table [" + table.getName() + "] without a row is not supported.");
        }

        addRows.execute();
    }

    @Override
    public void dropTable(Table table) {
        context.dropTable(getTableName(table)).execute();
    }


    @Override
    public void createView(View view) {
        context.execute(format("CREATE VIEW \"%s\" AS %s", view.getName(), view.getViewDefinition()));
    }

    @Override
    public void dropView(View view) {
        context.dropView(view.getName()).execute();
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void changeFieldType(Field oldField, Field newField, ch.ergon.adam.core.db.schema.DataType targetDataType) {
        DataType<?> newType = targetDataType == newField.getDataType() ? mapType(newField) : mapRawType(targetDataType);
        context.alterTable(oldField.getTable().getName()).alterColumn(oldField.getName()).set(newType).execute();
    }

    @Override
    public void dropConstraint(Constraint constraint) {
        context.alterTable(constraint.getTable().getName()).dropConstraint(constraint.getName()).execute();
    }

    @Override
    public void createConstraint(Constraint constraint) {
        if (constraint instanceof RuleConstraint) {
            RuleConstraint rule = (RuleConstraint)constraint;
            ConstraintEnforcementStep check = DSL.constraint(constraint.getName()).check(DSL.condition(rule.getRule()));
            context.alterTable(constraint.getTable().getName()).add(check).execute();
        } else if (constraint instanceof PrimaryKeyConstraint) {
            // Handled together with index
        } else {
            throw new RuntimeException("Not implemented");
        }
    }

    @Override
    public void dropSequence(Sequence sequence) {
        context.dropSequence(sequence.getName()).execute();
    }

    @Override
    public void createSequence(Sequence sequence) {
        CreateSequenceFlagsStep sequenceCreate = context.createSequence(sequence.getName());
        if (sequence.getStartValue() != null) {
            sequenceCreate.startWith(sequence.getStartValue());
        }
        if (sequence.getMinValue() != null) {
            sequenceCreate.minvalue(sequence.getMinValue());
        }
        if (sequence.getMaxValue() != null) {
            sequenceCreate.maxvalue(sequence.getMaxValue());
        }
        if (sequence.getIncrement() != null) {
            sequenceCreate.incrementBy(sequence.getIncrement());
        }
        sequenceCreate.execute();
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {
        table.getFields().stream()
            .filter(Field::isSequence)
            .forEach(field -> context.alterTable(table.getName())
                .alterColumn(field.getName())
                .dropDefault()
                .execute());

        //TODO: implement drop of sequences
    }

    @Override
    public void adjustSequences(Table table) {

    }

    @Override
    public void renameTable(Table oldTable, String targetTableName) {
        context.alterTable(getTableName(oldTable)).renameTo(targetTableName).execute();

    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {
        Map<String, Field> fieldsToCopy = targetTable.getFields().stream().filter(targetField -> sourceTable.getField(targetField.getName()) != null).collect(toLinkedMap(Field::getName, identity()));
        Map<String, Field> fieldsWithMigration = targetTable.getFields().stream()
            .filter(targetField -> sourceTable.getField(targetField.getName()) == null)
            .filter(targetField -> targetField.getSqlForNew() != null)
            .collect(toLinkedMap(Field::getName, identity()));

        String fieldNames = concat(
            fieldsToCopy.values().stream().map(Field::getName),
            fieldsWithMigration.values().stream().map(Field::getName)
        ).map(name -> "\"" + name + "\"").collect(joining(","));

        if (fieldNames.isEmpty()) {
            // Nothing to copy
            return;
        }

        String values = concat(
            fieldsToCopy.values().stream().map(field -> castIfNeeded(sourceTable.getField(field.getName()), field, context)),
            fieldsWithMigration.values().stream().map(field -> field.getSqlForNew())
        ).collect(joining(","));

        context.execute(format("INSERT INTO \"%s\" (%s) select %s from \"%s\" ",
            targetTable.getName(),
            fieldNames,
            values,
            sourceTableName
        ));
    }

    protected String castIfNeeded(Field sourceField, Field targetField, DSLContext renderContext) {
        if (sourceField.getDataType() == targetField.getDataType() && (sourceField.getDataType() != ENUM || sourceField.getDbEnum().equals(targetField.getDbEnum()))) {
            return "\"" + targetField.getName() + "\"";
        }
        DataType<?> targetType = mapType(targetField);
        org.jooq.Field<Object> jooqField = DSL.field("\"" + targetField.getName() + "\"");
        return renderContext.render(DSL.cast(jooqField, targetType));
    }

    protected DataType<?> mapType(Field field) {
        DataType jooqType = mapFieldToJooqType(field);
        if (field.getLength() != null && jooqType.hasLength()) {
            jooqType = jooqType.length(field.getLength());
        }
        if (field.isArray()) {
            jooqType = jooqType.getArrayDataType();
        }
        jooqType = jooqType.nullable(field.isNullable());
        jooqType = jooqType.identity(field.isSequence());
        if (field.getPrecision() != null && jooqType.hasPrecision() && jooqType.getSQLDataType() != TIMESTAMPWITHTIMEZONE) {
            jooqType = jooqType.precision(field.getPrecision());
        }
        if (field.getScale() != null && jooqType.hasScale()) {
            jooqType = jooqType.scale(field.getScale());
        }
        if (field.getDefaultValue() != null) {
            jooqType = jooqType.defaultValue(getDefaultValue(field));
        }
        return jooqType;

    }

    protected Object getDefaultValue(Field field) {
        return DSL.field(field.getDefaultValue());
    }

    protected DataType<?> mapFieldToJooqType(Field field) {
        return mapRawType(field.getDataType());
    }

    protected DataType<?> mapRawType(ch.ergon.adam.core.db.schema.DataType type) {
        DataType jooqType;
        switch (type) {
            case VARCHAR:
                jooqType = SQLDataType.VARCHAR;
                break;
            case CHAR:
                jooqType = SQLDataType.CHAR;
                break;
            case LONGVARCHAR:
                jooqType = SQLDataType.LONGVARCHAR;
                break;
            case CLOB:
                jooqType = SQLDataType.CLOB;
                break;
            case NVARCHAR:
                jooqType = SQLDataType.NVARCHAR;
                break;
            case NCHAR:
                jooqType = SQLDataType.NCHAR;
                break;
            case LONGNVARCHAR:
                jooqType = SQLDataType.LONGNVARCHAR;
                break;
            case NCLOB:
                jooqType = SQLDataType.NCLOB;
                break;
            case BOOLEAN:
                jooqType = SQLDataType.BOOLEAN;
                break;
            case BIT:
                jooqType = SQLDataType.BIT;
                break;
            case TINYINT:
                jooqType = SQLDataType.TINYINT;
                break;
            case SMALLINT:
                jooqType = SQLDataType.SMALLINT;
                break;
            case INTEGER:
                jooqType = SQLDataType.INTEGER;
                break;
            case BIGINT:
                jooqType = SQLDataType.BIGINT;
                break;
            case DECIMAL_INTEGER:
                jooqType = SQLDataType.DECIMAL_INTEGER;
                break;
            case TINYINTUNSIGNED:
                jooqType = SQLDataType.TINYINTUNSIGNED;
                break;
            case SMALLINTUNSIGNED:
                jooqType = SQLDataType.SMALLINTUNSIGNED;
                break;
            case INTEGERUNSIGNED:
                jooqType = SQLDataType.INTEGERUNSIGNED;
                break;
            case BIGINTUNSIGNED:
                jooqType = SQLDataType.BIGINTUNSIGNED;
                break;
            case DOUBLE:
                jooqType = SQLDataType.DOUBLE;
                break;
            case FLOAT:
                jooqType = SQLDataType.FLOAT;
                break;
            case REAL:
                jooqType = SQLDataType.REAL;
                break;
            case NUMERIC:
                jooqType = SQLDataType.NUMERIC;
                break;
            case DECIMAL:
                jooqType = SQLDataType.DECIMAL;
                break;
            case DATE:
                jooqType = SQLDataType.DATE;
                break;
            case TIMESTAMP:
                jooqType = SQLDataType.TIMESTAMP;
                break;
            case TIME:
                jooqType = SQLDataType.TIME;
                break;
            case INTERVALYEARTOSECOND:
                jooqType = SQLDataType.INTERVAL;
                break;
            case INTERVALYEARTOMONTH:
                jooqType = SQLDataType.INTERVALYEARTOMONTH;
                break;
            case INTERVALDAYTOSECOND:
                jooqType = SQLDataType.INTERVALDAYTOSECOND;
                break;
            case LOCALDATE:
                jooqType = SQLDataType.LOCALDATE;
                break;
            case LOCALTIME:
                jooqType = SQLDataType.LOCALTIME;
                break;
            case LOCALDATETIME:
                jooqType = SQLDataType.LOCALDATETIME;
                break;
            case OFFSETTIME:
                jooqType = SQLDataType.OFFSETTIME;
                break;
            case OFFSETDATETIME:
                jooqType = SQLDataType.OFFSETDATETIME;
                break;
            case TIMEWITHTIMEZONE:
                jooqType = SQLDataType.TIMEWITHTIMEZONE;
                break;
            case TIMESTAMPWITHTIMEZONE:
                jooqType = TIMESTAMPWITHTIMEZONE;
                break;
            case BINARY:
                jooqType = SQLDataType.BINARY;
                break;
            case VARBINARY:
                jooqType = SQLDataType.VARBINARY;
                break;
            case LONGVARBINARY:
                jooqType = SQLDataType.LONGVARBINARY;
                break;
            case BLOB:
                jooqType = SQLDataType.BLOB;
                break;
            case UUID:
                jooqType = SQLDataType.UUID;
                break;
            default:
                throw new RuntimeException("Unsupported datatype [" + type + "]");
        }
        return jooqType.getDataType(context.configuration());
    }
}

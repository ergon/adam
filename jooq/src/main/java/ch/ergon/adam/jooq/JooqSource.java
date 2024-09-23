package ch.ergon.adam.jooq;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.DataType;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.ForeignKey;
import ch.ergon.adam.core.db.schema.Index;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import org.jooq.types.YearToMonth;
import org.jooq.types.YearToSecond;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class JooqSource implements SchemaSource {

    private final Connection connection;
    private final String schemaName;
    private DSLContext context;
    private Meta meta;
    private SQLDialect sqlDialect = SQLDialect.DEFAULT;

    public JooqSource(String url, String schemaName) throws SQLException {
        connection = DriverManager.getConnection(url);
        this.schemaName = schemaName;
    }

    public JooqSource(Connection connection, String schemaName) {
        this.connection = connection;
        this.schemaName = schemaName;
    }

    public JooqSource(String url) throws SQLException {
        connection = DriverManager.getConnection(url);
        schemaName = null;
    }

    public JooqSource(Connection connection) {
        this.connection = connection;
        schemaName = null;
    }

    protected void setSqlDialect(SQLDialect dialect) {
        if (this.context != null) {
            throw new IllegalStateException("Context has already been created");
        }
        this.sqlDialect = dialect;
    }

    protected Meta getMeta() {
        if (meta == null) {
            meta = extractMeta(schemaName);
        }
        return meta;
    }

    protected DSLContext getContext() {
        if (context == null) {
            context = DSL.using(connection, sqlDialect);
        }
        return context;
    }

    private Meta extractMeta(String schemaName) {
        if (schemaName == null) {
            return getContext().meta();
        }
        List<org.jooq.Schema> schemas = getContext().meta().getSchemas(schemaName);
        if (schemas.isEmpty()) {
            String knownSchemas = getContext().meta().getSchemas().stream().map(Named::getName).collect(Collectors.joining(","));
            throw new RuntimeException("Schema [" + schemaName + "] not found. Known schemas are [" + knownSchemas + "]");
        }
        return getContext().meta(schemas.get(0));
    }

    @Override
    public void close() {
        if (context instanceof CloseableDSLContext) {
            ((CloseableDSLContext) context).close();
        }
    }

    @Override
    public Schema getSchema() {
        // Clear jooq caches
        context = null;
        meta = null;

        Schema schema = new Schema();
        schema.setTables(getTables());
        return schema;
    }

    private Collection<Table> getTables() {
        List<org.jooq.Table<?>> jooqTables = getMeta().getTables();

        Map<String, Table> tables = jooqTables.stream()
            .map(this::mapTableFromJooq)
            .sorted(comparing(Table::getName))
            .collect(toLinkedMap(Table::getName, identity()));

        mapForeignKeys(jooqTables, tables);
        return tables.values();
    }

    private void mapForeignKeys(List<org.jooq.Table<?>> jooqTables, Map<String, Table> tables) {
        for (org.jooq.Table<?> jooqTable : jooqTables) {
            Table table = tables.get(jooqTable.getName());
            table.setForeignKeys(jooqTable.getReferences().stream().map(fk -> mapForeignKeyFromJooq(tables, fk)).collect(toList()));
        }

    }

    private ForeignKey mapForeignKeyFromJooq(Map<String, Table> tables, org.jooq.ForeignKey<?,?> jooqForeignKey) {
        Table table = tables.get(jooqForeignKey.getTable().getName());
        ForeignKey foreignKey = new ForeignKey(jooqForeignKey.getName());
        if (jooqForeignKey.getFields().size() != 1) {
            throw new RuntimeException("Table [" + table.getName() + "] contains a foreign key over multiple fields. This is not yet supported.");
        }
        foreignKey.setField(table.getField(jooqForeignKey.getFields().get(0).getName()));
        Table foreignTable = tables.get(jooqForeignKey.getKey().getTable().getName());
        foreignKey.setTargetIndex(foreignTable.getIndex(jooqForeignKey.getKey().getName()));
        return foreignKey;
    }

    private Table mapTableFromJooq(org.jooq.Table<?> jooqTable) {
        Table table = new Table(jooqTable.getName());
        table.setFields(stream(jooqTable.fields()).map(this::mapFieldFromJooq).collect(toList()));
        List<Index> indexes = jooqTable.getIndexes().stream().map(jooqIndex -> mapIndexFromJooq(table, jooqIndex)).collect(toList());
        if (jooqTable.getPrimaryKey() != null) {
            indexes.add(mapPrimaryKeyFromJooq(table, jooqTable.getPrimaryKey()));
        }
        jooqTable.getUniqueKeys().stream().map(jooqKey -> mapUniqueKeyFromJooq(table, jooqKey)).forEach(indexes::add);
        table.setIndexes(indexes);
        return table;
    }

    private Index mapPrimaryKeyFromJooq(Table table, UniqueKey<?> primaryKey) {
        Index index = mapUniqueKeyFromJooq(table, primaryKey);
        index.setPrimary(true);
        return index;
    }

    private Index mapUniqueKeyFromJooq(Table table, UniqueKey<?> uniqueKey) {
        Index index = mapKeyFromJooq(table, uniqueKey);
        index.setUnique(true);
        return index;
    }

    private Index mapIndexFromJooq(Table table, org.jooq.Index jooqIndex) {
        Index index = new Index(jooqIndex.getName());
        if (jooqIndex.getWhere() != null) {
            index.setWhere(jooqIndex.getWhere().toString());
        }
        index.setUnique(jooqIndex.getUnique());
        index.setFields(jooqIndex.getFields().stream().map(jooqField -> table.getField(jooqField.getName())).collect(toList()));
        return index;
    }

    private Index mapKeyFromJooq(Table table, Key<?> jooqKey) {
        Index index = new Index(jooqKey.getName());
        index.setFields(jooqKey.getFields().stream().map(jooqField -> table.getField(jooqField.getName())).collect(toList()));
        return index;
    }

    private Field mapFieldFromJooq(org.jooq.Field<?> jooqField) {
        Field field = new Field(jooqField.getName());
        field.setArray(jooqField.getDataType().isArray());
        field.setDataType(mapDataTypeFromJooq(jooqField));
        org.jooq.DataType<?> jooqType = jooqField.getDataType(getContext().configuration());
        field.setNullable(jooqType.nullable());

        org.jooq.DataType<?> elementType;

        if (field.isArray()) {
            elementType = jooqType.getArrayComponentDataType();
        } else {
            elementType = jooqType;
        }

        field.setLength(elementType.hasLength() && elementType.length() > 0 && elementType.length() < 20000000 ? elementType.length() : null);
        field.setPrecision(elementType.hasPrecision() && elementType.precision() > 0 && elementType.precision() < 10000 ? elementType.precision() : null);
        field.setScale(elementType.hasScale() && elementType.scale() > 0 ? elementType.scale() : null);


        field.setSequence(isSequence(jooqField));
        if (!field.isSequence() && jooqField.getDataType().defaulted()) {
            field.setDefaultValue(getDefaultValue(jooqField));
        }
        return field;
    }

    protected String getDefaultValue(org.jooq.Field<?> jooqField) {
        org.jooq.Field<?> defaultValue = jooqField.getDataType().defaultValue();
        return defaultValue.toString();
    }

    protected boolean isSequence(org.jooq.Field<?> jooqField) {
        return jooqField.getDataType().identity();
    }

    protected DataType mapDataTypeFromJooq(org.jooq.Field<?> jooqField) {
        org.jooq.DataType<?> sqlDataType = jooqField.getDataType().getSQLDataType();
        if (sqlDataType.isInterval()) {
            Class<?> type = sqlDataType.getType();
            if (type.equals(YearToSecond.class)) {
                return DataType.INTERVALYEARTOSECOND;
            } else if (type.equals(YearToMonth.class)) {
                return DataType.INTERVALYEARTOMONTH;
            } else if (type.equals(DayToSecond.class)) {
                return DataType.INTERVALDAYTOSECOND;
            } else {
                throw new RuntimeException("Unsupported interval type [" + type.getName() + "]");
            }
        } else if (sqlDataType.isNumeric() && jooqField.getDataType().precision() == 0 && jooqField.getDataType().scale() == 0) {
            return DataType.DECIMAL_INTEGER;
        }
        String typeName = jooqField.getDataType().isArray() ? sqlDataType.getArrayBaseDataType().getTypeName() : sqlDataType.getTypeName();
        try {
            return DataType.valueOf(typeName.toUpperCase().replace(" ", ""));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown type [" + typeName + "]");
        }
    }
}

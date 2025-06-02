package ch.ergon.adam.mariadb;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.jooq.JooqSink;
import org.jooq.CreateTableElementListStep;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;

import static org.jooq.SQLDialect.MARIADB;
import static org.jooq.impl.SQLDataType.VARCHAR;

public class MariaDbSink extends JooqSink {

    private String schemaName;

    public MariaDbSink(Connection dbConnection, String schema) {
        super(dbConnection, MARIADB, schema);
        this.schemaName = schema;
    }

    @Override
    public void createTable(Table table) {
        CreateTableElementListStep createTable = context.createTable(getTableName(table));
        CreateTableElementListStep addRows = null;
        for (Field field : table.getFields()) {
            if (addRows == null) {
                CreateTableElementListStep column = createTable.column(field.getName(), mapType(field));
                if (table.getIndexes().stream().anyMatch(it -> it.isPrimary() && it.getFields().contains(field))) {
                    column = column.primaryKey(field.getName());
                }
                addRows = column;
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
    public void dropConstraint(Constraint constraint) {
        if (constraint instanceof PrimaryKeyConstraint) {
            return;
        }
        super.dropConstraint(constraint);
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {
        table.getFields().stream()
            .filter(Field::isSequence)
            .filter(f -> f.getReferencingIndexes().stream().noneMatch(Index::isPrimary))
            .forEach(field -> context.alterTable(table.getName())
                .alterColumn(field.getName())
                .dropDefault()
                .execute());
    }

    @Override
    public void dropIndex(Index index) {
        if (index.isPrimary()) {
            context.alterTable(getTableName(index.getTable()))
                .dropPrimaryKey()
                .execute();
        } else {
            super.dropIndex(index);
        }
    }

    @Override
    public void createIndex(Index index) {
        if (index.isPrimary()) {
            return;
        }
        super.createIndex(index);
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        // In MariaDB ENUM types are defined as part of the column definition, not as separate types
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        // In MariaDB ENUM types are defined as part of the column definition, not as separate types
    }

    @Override
    protected org.jooq.DataType<?> mapFieldToJooqType(Field field) {
        switch (field.getDataType()) {
            case TIMESTAMPWITHTIMEZONE:
                return SQLDataType.TIMESTAMP(6);
            case ENUM:
                return new DefaultDataType<>(null, VARCHAR, field.getDbEnum().getName(), field.getDbEnum().getName());
            default:
                return super.mapFieldToJooqType(field);
        }
    }
}

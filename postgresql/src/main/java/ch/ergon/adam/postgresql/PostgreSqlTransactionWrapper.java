package ch.ergon.adam.postgresql;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.jooq.JooqSqlExecutor;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSqlTransactionWrapper implements SchemaSource, SchemaSink, SqlExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlTransactionWrapper.class);

    private final Connection dbConnection;
    private final Runnable closeHandler;
    JooqSqlExecutor sqlExecutor;
    PostgreSqlSink sqlSink;
    PostgreSqlSource sqlSource;
    private boolean closed;
    private int clientCount = 0;

    public PostgreSqlTransactionWrapper(String url, String schema, Runnable closeHandler) {
        this.closeHandler = closeHandler;
        try {
            dbConnection = DriverManager.getConnection(url);
            sqlSink = new PostgreSqlSink(dbConnection, schema);
            sqlSource = new PostgreSqlSource(dbConnection, schema);
            beginTransaction();
            sqlExecutor = new JooqSqlExecutor(dbConnection, schema);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void increaseClientCount() {
        clientCount++;
    }

    public void decreaseClientCount() {
        clientCount--;
    }

    public int getClientCount() {
        return clientCount;
    }

    public void beginTransaction() {
        try {
            dbConnection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commitTransaction() {
        try {
            logger.info("Doing a commit of the changes.");
            dbConnection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            if (dbConnection.isClosed()) {
                return;
            }
            logger.info("Doing a rollback of the changes.");
            dbConnection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTargetSchema(Schema targetSchema) {
        sqlSink.setTargetSchema(targetSchema);
    }

    @Override
    public void commitChanges() {
        sqlSink.commitChanges();
    }

    @Override
    public void rollback() {
        rollbackTransaction();
    }

    @Override
    public void dropForeignKey(ForeignKey foreignKey) {
        sqlSink.dropForeignKey(foreignKey);
    }

    @Override
    public void createForeignKey(ForeignKey foreignKey) {
        sqlSink.createForeignKey(foreignKey);
    }

    @Override
    public void dropIndex(Index index) {
        sqlSink.dropIndex(index);
    }

    @Override
    public void createIndex(Index index) {
        sqlSink.createIndex(index);
    }

    @Override
    public void addField(Field field) {
        sqlSink.addField(field);
    }

    @Override
    public void dropField(Field field, Table table) {
        sqlSink.dropField(field, table);
    }

    @Override
    public void setDefault(Field field) {
        sqlSink.setDefault(field);
    }

    @Override
    public void dropDefault(Field field) {
        sqlSink.dropDefault(field);
    }

    @Override
    public void createTable(Table table) {
        sqlSink.createTable(table);
    }

    @Override
    public void dropTable(Table table) {
        sqlSink.dropTable(table);
    }

    @Override
    public void renameTable(Table oldTable, String targetTableName) {
        sqlSink.renameTable(oldTable, targetTableName);
    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {
        sqlSink.copyData(sourceTable, targetTable, sourceTableName);
    }

    @Override
    public void createView(View view) {
        sqlSink.createView(view);
    }

    @Override
    public void dropView(View view) {
        sqlSink.dropView(view);
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        sqlSink.dropEnum(dbEnum);
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        sqlSink.createEnum(dbEnum);
    }

    @Override
    public void changeFieldType(Field oldField, Field newField, DataType targetDataType) {
        sqlSink.changeFieldType(oldField, newField, targetDataType);
    }

    @Override
    public void dropConstraint(Constraint constraint) {
        sqlSink.dropConstraint(constraint);
    }

    @Override
    public void createConstraint(Constraint constraint) {
        sqlSink.createConstraint(constraint);
    }

    @Override
    public void dropSequence(Sequence sequence) {
        sqlSink.dropSequence(sequence);
    }

    @Override
    public void createSequence(Sequence sequence) {
        sqlSink.createSequence(sequence);
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {
        sqlSink.dropSequencesAndDefaults(table);
    }

    @Override
    public void adjustSequences(Table table) {
        sqlSink.adjustSequences(table);
    }

    @Override
    public void executeScript(String script) {
        sqlExecutor.executeScript(script);
    }

    @Override
    public Object queryResult(String query, Object... params) {
        return sqlExecutor.queryResult(query, params);
    }

    @Override
    public void dropSchema() {
        sqlExecutor.dropSchema();
    }

    @Override
    public void close() {
        closeHandler.run();
    }

    public void reallyClose() {
        this.closed = true;
        try {
            if (!dbConnection.getAutoCommit()) {
                commitTransaction();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sqlExecutor.close();
        sqlSink.close();
        sqlSource.close();
        try {
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean supportAlterAndDropField() {
        return sqlSink.supportAlterAndDropField();
    }

    @Override
    public Schema getSchema() {
        return sqlSource.getSchema();
    }

    public Connection getConnection() {
        return dbConnection;
    }
}

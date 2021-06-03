package ch.ergon.adam.core.db;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSinkWrapper implements SchemaSink {

    private final Logger logger;

    private final SchemaSink wrappedSink;

    public LoggingSinkWrapper(SchemaSink wrappedSink) {
        this.wrappedSink = wrappedSink;
        logger = LoggerFactory.getLogger(wrappedSink.getClass());
    }

    @Override
    public void setTargetSchema(Schema targetSchema) {
        wrappedSink.setTargetSchema(targetSchema);
    }

    @Override
    public void commitChanges() {
        logger.info("Commit schema changes.");
        wrappedSink.commitChanges();
    }

    @Override
    public void rollback() {

    }

    @Override
    public void dropForeignKey(ForeignKey foreignKey) {
        logger.info("Drop FK [{}]", foreignKey.getName());
        wrappedSink.dropForeignKey(foreignKey);
    }

    @Override
    public void createForeignKey(ForeignKey foreignKey) {
        logger.info("Create FK [{}]", foreignKey.getName());
        wrappedSink.createForeignKey(foreignKey);
    }

    @Override
    public void dropIndex(Index index) {
        logger.info("Drop index [{}]", index.getName());
        wrappedSink.dropIndex(index);
    }

    @Override
    public void createIndex(Index index) {
        logger.info("Create index [{}]", index.getName());
        wrappedSink.createIndex(index);
    }

    @Override
    public void addField(Field field) {
        logger.info("Create field [{}]", field.getName());
        wrappedSink.addField(field);
    }

    @Override
    public void dropField(Field field, Table table) {
        logger.info("Drop field [{}] from table [{}]", field.getName(), table.getName());
        wrappedSink.dropField(field, table);
    }

    @Override
    public void setDefault(Field field) {
        logger.info("Set default [{}] for field [{}] on table [{}]", field.getDefaultValue(), field.getName(), field.getTable().getName());
        wrappedSink.setDefault(field);
    }

    @Override
    public void dropDefault(Field field) {
        logger.info("Drop default from field [{}] on table [{}]", field.getName(), field.getTable().getName());
        wrappedSink.dropDefault(field);
    }

    @Override
    public void createTable(Table table) {
        logger.info("Create table [{}]", table.getName());
        wrappedSink.createTable(table);
    }

    @Override
    public void dropTable(Table table) {
        logger.info("Drop table [{}]", table.getName());
        wrappedSink.dropTable(table);
    }

    @Override
    public void renameTable(Table oldTable, String newTableName) {
        logger.info("Rename table [{}] to [{}]", oldTable.getName(), newTableName);
        wrappedSink.renameTable(oldTable, newTableName);
    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {
        logger.info("Copy data from table [{}] to [{}]", sourceTableName, targetTable.getName());
        wrappedSink.copyData(sourceTable, targetTable, sourceTableName);
    }

    @Override
    public void createView(View view) {
        logger.info("Create view [{}]", view.getName());
        wrappedSink.createView(view);
    }

    @Override
    public void dropView(View view) {
        logger.info("Drop view [{}]", view.getName());
        wrappedSink.dropView(view);
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        logger.info("Drop enum [{}]", dbEnum.getName());
        wrappedSink.dropEnum(dbEnum);
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        logger.info("Create enum [{}]", dbEnum.getName());
        wrappedSink.createEnum(dbEnum);
    }

    @Override
    public void changeFieldType(Field oldField, Field newField, DataType targetDataType) {
        logger.info("Change type of field [{}.{}] to [{}]", oldField.getTable().getName(), oldField.getName(), targetDataType.name());
        wrappedSink.changeFieldType(oldField, newField, targetDataType);
    }

    @Override
    public void dropConstraint(Constraint constraint) {
        logger.info("Drop constraint [{}]", constraint.getName());
        wrappedSink.dropConstraint(constraint);
    }

    @Override
    public void createConstraint(Constraint constraint) {
        logger.info("Create constraint [{}]", constraint.getName());
        wrappedSink.createConstraint(constraint);
    }

    @Override
    public void dropSequence(Sequence sequence) {
        logger.info("Drop sequence [{}]", sequence.getName());
        wrappedSink.dropSequence(sequence);
    }

    @Override
    public void createSequence(Sequence sequence) {
        logger.info("Create sequence [{}]", sequence.getName());
        wrappedSink.createSequence(sequence);
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {
        logger.info("Drop sequences and default for table [{}]", table.getName());
        wrappedSink.dropSequencesAndDefaults(table);
    }

    @Override
    public void close() throws Exception {
        wrappedSink.close();
    }

    @Override
    public boolean supportAlterAndDropField() {
        return wrappedSink.supportAlterAndDropField();
    }
}

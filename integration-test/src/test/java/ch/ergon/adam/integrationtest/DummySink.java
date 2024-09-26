package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;

public class DummySink implements SchemaSink {

    private Schema targetSchema;
    private boolean committed = false;

    @Override
    public void setTargetSchema(Schema targetSchema) {
        this.targetSchema = targetSchema;
    }

    public Schema getTargetSchema() {
        return committed ? targetSchema : null;
    }

    @Override
    public void commitChanges() {
        committed = true;
    }

    @Override
    public void rollback() {

    }

    @Override
    public void dropForeignKey(ForeignKey foreignKey) {

    }

    @Override
    public void createForeignKey(ForeignKey foreignKey) {

    }

    @Override
    public void dropIndex(Index index) {

    }

    @Override
    public void createIndex(Index index) {

    }

    @Override
    public void addField(Field field) {

    }

    @Override
    public void dropField(Field field, Table table) {

    }

    @Override
    public void setDefault(Field field) {

    }

    @Override
    public void dropDefault(Field field) {

    }

    @Override
    public void createTable(Table table) {

    }

    @Override
    public void dropTable(Table table) {

    }

    @Override
    public void renameTable(Table oldTable, String tempTableName) {

    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {

    }

    @Override
    public void createView(View view) {

    }

    @Override
    public void dropView(View view) {

    }

    @Override
    public void dropEnum(DbEnum dbEnum) {

    }

    @Override
    public void createEnum(DbEnum dbEnum) {

    }

    @Override
    public void changeFieldType(Field oldField, Field newField, DataType targetDataType) {

    }

    @Override
    public void dropConstraint(Constraint constraint) {

    }

    @Override
    public void createConstraint(Constraint constraint) {

    }

    @Override
    public void dropSequence(Sequence sequence) {

    }

    @Override
    public void createSequence(Sequence sequence) {

    }

    @Override
    public void dropSequencesAndDefaults(Table table) {

    }

    @Override
    public void adjustSequences(Table table) {

    }

    @Override
    public void close() {

    }
}

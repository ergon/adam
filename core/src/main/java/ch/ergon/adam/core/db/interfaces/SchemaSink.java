package ch.ergon.adam.core.db.interfaces;

import ch.ergon.adam.core.db.schema.*;

public interface SchemaSink extends AutoCloseable {

    void setTargetSchema(Schema targetSchema);

    void commitChanges();

    void rollback();

    void dropForeignKey(ForeignKey foreignKey);

    void createForeignKey(ForeignKey foreignKey);

    void dropIndex(Index index);

    void createIndex(Index index);

    void addField(Field field);

    void dropField(Field field, Table table);

    void setDefault(Field field);

    void dropDefault(Field field);

    void createTable(Table table);

    void dropTable(Table table);

    void renameTable(Table oldTable, String targetTableName);

    void copyData(Table sourceTable, Table targetTable, String sourceTableName);

    void createView(View view);

    void dropView(View view);

    void dropEnum(DbEnum dbEnum);

    void createEnum(DbEnum dbEnum);

    void changeFieldType(Field oldField, Field newField, DataType targetDataType);

    void dropConstraint(Constraint constraint);

    void createConstraint(Constraint constraint);

    void dropSequence(Sequence sequence);

    void createSequence(Sequence sequence);

    void dropSequencesAndDefaults(Table table);

    default boolean supportAlterAndDropField() {
        return true;
    }
}

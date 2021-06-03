package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.db.interfaces.MigrationStrategy;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;

import static org.junit.jupiter.api.Assertions.fail;

public class AssertAnyChangeStrategy implements MigrationStrategy {
    @Override
    public void tableAdded(Table newTable) {
        fail("Found new table [" + newTable.getName() +"]");
    }

    @Override
    public void tableRenamed(Table oldTable, Table newTable) {
        fail("Found renamed table [" + newTable.getName() +"]");
    }

    @Override
    public void tableRemoved(Table oldTable) {
        fail("Found removed table [" + oldTable.getName() +"]");
    }

    @Override
    public void fieldAdded(Field newField) {
        fail("Found added table [" + newField.getName() +"]");
    }

    @Override
    public void fieldRemoved(Field oldField) {
        fail("Found removed table [" + oldField.getName() +"]");
    }

    @Override
    public void fieldRenamed(Field oldField, Field newField) {
        fail("Found renamed table [" + newField.getName() +"]");
    }

    @Override
    public void fieldIndexChange(Field oldField, Field newField) {
        fail("Field changed index [" + newField.getName() +"]");
    }

    @Override
    public void fileTypeChanged(Field oldField, Field newField) {
        fail("Field changed type [" + newField.getName() +"]");
    }

    @Override
    public void fieldDefaultChanged(Field oldField, Field newField) {
        fail("Field changed default [" + newField.getName() +"]");
    }

    @Override
    public void indexAdded(Index newIndex) {
        fail("Found new index [" + newIndex.getName() +"]");
    }

    @Override
    public void indexUpdated(Index oldIndex, Index newIndex) {
        fail("Found updated index [" + newIndex.getName() +"]");
    }

    @Override
    public void indexRemoved(Index oldIndex) {
        fail("Found removed index [" + oldIndex.getName() +"]");
    }

    @Override
    public void foreignKeyAdded(ForeignKey newForeignKey) {
        fail("Found new fk [" + newForeignKey.getName() +"]");
    }

    @Override
    public void foreignKeyUpdated(ForeignKey oldForeignKey, ForeignKey newForeignKey) {
        fail("Found updated fk [" + newForeignKey.getName() +"]");
    }

    @Override
    public void foreignKeyRemoved(ForeignKey oldForeignKey) {
        fail("Found removed fk [" + oldForeignKey.getName() +"]");
    }

    @Override
    public void viewAdded(View newView) {
        fail("Found new view [" + newView.getName() +"]");
    }

    @Override
    public void viewRemoved(View oldView) {
        fail("Found removed view [" + oldView.getName() +"]");
    }

    @Override
    public void viewUpdated(View oldView, View newView) {
        fail("Found updated view [" + newView.getName() +"]");
    }

    @Override
    public void apply(SchemaSink sink) {

    }

    @Override
    public void setSourceSchema(Schema sourceSchema) {

    }

    @Override
    public void setTargetSchema(Schema targetSchema) {

    }

    @Override
    public void enumAdded(DbEnum newEnum) {
        fail("Found new enum [" + newEnum.getName() +"]");
    }

    @Override
    public void enumRemoved(DbEnum oldEnum) {
        fail("Found removed enum [" + oldEnum.getName() +"]");
    }

    @Override
    public void enumUpdated(DbEnum oldEnum, DbEnum newEnum) {
        fail("Found updated enum [" + newEnum.getName() +"]");
    }

    @Override
    public void constraintAdded(Constraint newConstraint) {
        fail("Found new constraint [" + newConstraint.getName() +"]");
    }

    @Override
    public void constraintRemoved(Constraint oldConstraint) {
        if (oldConstraint instanceof PrimaryKeyConstraint) {
            //Ignore
            return;
        }
        fail("Found removed constraint [" + oldConstraint.getName() +"]");
    }

    @Override
    public void constraintUpdated(Constraint oldConstraint, Constraint newConstraint) {
        fail("Found updated constraint [" + newConstraint.getName() +"]");
    }

    @Override
    public void sequenceAdded(Sequence sequence) {
        fail("Found new sequence [" + sequence.getName() +"]");
    }

    @Override
    public void sequenceRemoved(Sequence sequence) {
        fail("Found removed sequence [" + sequence.getName() +"]");
    }

    @Override
    public void sequenceUpdated(Sequence sourceSequence, Sequence targetSequence) {
        fail("Found updated sequence [" + targetSequence.getName() +"]");
    }
}

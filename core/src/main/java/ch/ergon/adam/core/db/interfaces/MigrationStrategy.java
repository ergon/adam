package ch.ergon.adam.core.db.interfaces;

import ch.ergon.adam.core.db.schema.*;

public interface MigrationStrategy {

    void tableAdded(Table newTable);

    void tableRenamed(Table oldTable, Table newTable);

    void tableRemoved(Table oldTable);

    void fieldAdded(Field newField);

    void fieldRemoved(Field oldField);

    void fieldRenamed(Field oldField, Field newField);

    void fieldIndexChange(Field oldField, Field newField);

    void fileTypeChanged(Field oldField, Field newField);

    void fieldDefaultChanged(Field oldField, Field newField);

    void indexAdded(Index newIndex);

    void indexUpdated(Index oldIndex, Index newIndex);

    void indexRemoved(Index oldIndex);

    void foreignKeyAdded(ForeignKey newForeignKey);

    void foreignKeyUpdated(ForeignKey oldForeignKey, ForeignKey newForeignKey);

    void foreignKeyRemoved(ForeignKey oldForeignKey);

    void viewAdded(View newView);

    void viewRemoved(View oldView);

    void viewUpdated(View oldView, View newView);

    void apply(SchemaSink sink);

    void setSourceSchema(Schema sourceSchema);

    void setTargetSchema(Schema targetSchema);

    void enumAdded(DbEnum newEnum);

    void enumRemoved(DbEnum oldEnum);

    void enumUpdated(DbEnum oldEnum, DbEnum newEnum);

    void constraintAdded(Constraint newConstraint);

    void constraintRemoved(Constraint oldConstraint);

    void constraintUpdated(Constraint oldConstraint, Constraint newConstraint);

    void sequenceAdded(Sequence sequence);

    void sequenceRemoved(Sequence sequence);

    void sequenceUpdated(Sequence sourceSequence, Sequence targetSequence);
}

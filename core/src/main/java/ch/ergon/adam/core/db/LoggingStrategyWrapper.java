package ch.ergon.adam.core.db;

import ch.ergon.adam.core.db.interfaces.MigrationStrategy;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingStrategyWrapper implements MigrationStrategy {

    private final Logger logger;

    private final MigrationStrategy wrappedStrategy;

    public LoggingStrategyWrapper(MigrationStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
        logger = LoggerFactory.getLogger(wrappedStrategy.getClass());
    }

    @Override
    public void tableAdded(Table newTable) {
        logger.info("Table [{}] added", newTable.getName());
        wrappedStrategy.tableAdded(newTable);
    }

    @Override
    public void tableRenamed(Table oldTable, Table newTable) {
        logger.info("Table [{}] renamed to [{}]", oldTable.getName(), newTable.getName());
        wrappedStrategy.tableRenamed(oldTable, newTable);
    }

    @Override
    public void tableRemoved(Table oldTable) {
        logger.info("Table [{}] removed", oldTable.getName());
        wrappedStrategy.tableRemoved(oldTable);
    }

    @Override
    public void fieldAdded(Field newField) {
        logger.info("Field [{}] added to table [{}]", newField.getName(), newField.getTable().getName());
        wrappedStrategy.fieldAdded(newField);
    }

    @Override
    public void fieldRemoved(Field oldField) {
        logger.info("Field [{}] removed from table [{}]", oldField.getName(), oldField.getTable().getName());
        wrappedStrategy.fieldRemoved(oldField);
    }

    @Override
    public void fieldRenamed(Field oldField, Field newField) {
        logger.info("Field [{}] renamed to [{}] on table [{}]", oldField.getName(), newField.getName(), newField.getTable().getName());
        wrappedStrategy.fieldRenamed(oldField, newField);
    }

    @Override
    public void fieldIndexChange(Field oldField, Field newField) {
        logger.info("Field [{}] index changed from [{}] to [{}] on table [{}]", newField.getName(), oldField.getIndex(), newField.getIndex(), newField.getTable().getName());
        wrappedStrategy.fieldIndexChange(oldField, newField);
    }

    @Override
    public void fileTypeChanged(Field oldField, Field newField) {
        logger.info("Field [{}] type changed from [{}] to [{}] on table [{}]", newField.getName(), oldField.getTypeDescription(), newField.getTypeDescription(), newField.getTable().getName());
        wrappedStrategy.fileTypeChanged(oldField, newField);
    }

    @Override
    public void fieldDefaultChanged(Field oldField, Field newField) {
        logger.info("Field [{}] default changed from [{}] to [{}] on table [{}]", newField.getName(), oldField.getDefaultValue(), newField.getDefaultValue(), newField.getTable().getName());
        wrappedStrategy.fieldDefaultChanged(oldField, newField);
    }

    @Override
    public void indexAdded(Index newIndex) {
        logger.info("Index [{}] added", newIndex.getName());
        wrappedStrategy.indexAdded(newIndex);
    }

    @Override
    public void indexUpdated(Index oldIndex, Index newIndex) {
        logger.info("Index [{}] updated", newIndex.getName());
        wrappedStrategy.indexUpdated(oldIndex, newIndex);
    }

    @Override
    public void indexRemoved(Index oldIndex) {
        logger.info("Index [{}] removed", oldIndex.getName());
        wrappedStrategy.indexRemoved(oldIndex);
    }

    @Override
    public void foreignKeyAdded(ForeignKey newForeignKey) {
        logger.info("FK [{}] added", newForeignKey.getName());
        wrappedStrategy.foreignKeyAdded(newForeignKey);
    }

    @Override
    public void foreignKeyUpdated(ForeignKey oldForeignKey, ForeignKey newForeignKey) {
        logger.info("FK [{}] updated", newForeignKey.getName());
        wrappedStrategy.foreignKeyUpdated(oldForeignKey, newForeignKey);
    }

    @Override
    public void foreignKeyRemoved(ForeignKey oldForeignKey) {
        logger.info("FK [{}] removed", oldForeignKey.getName());
        wrappedStrategy.foreignKeyRemoved(oldForeignKey);
    }

    @Override
    public void viewAdded(View newView) {
        logger.info("View [{}] added", newView.getName());
        wrappedStrategy.viewAdded(newView);
    }

    @Override
    public void viewRemoved(View oldView) {
        logger.info("View [{}] removed", oldView.getName());
        wrappedStrategy.viewRemoved(oldView);
    }

    @Override
    public void viewUpdated(View oldView, View newView) {
        logger.info("View [{}] updated", newView.getName());
        wrappedStrategy.viewUpdated(oldView, newView);
    }

    @Override
    public void apply(SchemaSink sink) {
        logger.info("Apply schema changes");
        wrappedStrategy.apply(sink);
    }

    @Override
    public void setSourceSchema(Schema sourceSchema) {
        wrappedStrategy.setSourceSchema(sourceSchema);
    }

    @Override
    public void setTargetSchema(Schema targetSchema) {
        wrappedStrategy.setTargetSchema(targetSchema);
    }

    @Override
    public void enumAdded(DbEnum newEnum) {
        logger.info("Enum [{}] added", newEnum.getName());
        wrappedStrategy.enumAdded(newEnum);
    }

    @Override
    public void enumRemoved(DbEnum oldEnum) {
        logger.info("Enum [{}] removed", oldEnum.getName());
        wrappedStrategy.enumRemoved(oldEnum);
    }

    @Override
    public void enumUpdated(DbEnum oldEnum, DbEnum newEnum) {
        logger.info("Enum [{}] updated", newEnum.getName());
        wrappedStrategy.enumUpdated(oldEnum, newEnum);
    }

    @Override
    public void constraintAdded(Constraint newConstraint) {
        logger.info("Constraint [{}] added on table [{}]", newConstraint.getName(), newConstraint.getTable().getName());
        wrappedStrategy.constraintAdded(newConstraint);
    }

    @Override
    public void constraintRemoved(Constraint oldConstraint) {
        logger.info("Constraint [{}] removed from table [{}]", oldConstraint.getName(), oldConstraint.getTable().getName());
        wrappedStrategy.constraintRemoved(oldConstraint);
    }

    @Override
    public void constraintUpdated(Constraint oldConstraint, Constraint newConstraint) {
        logger.info("Constraint [{}] updated on table [{}]", newConstraint.getName(), newConstraint.getTable().getName());
        wrappedStrategy.constraintUpdated(oldConstraint, newConstraint);
    }

    @Override
    public void sequenceAdded(Sequence sequence) {
        logger.info("Sequence [{}] added", sequence.getName());
        wrappedStrategy.sequenceAdded(sequence);
    }

    @Override
    public void sequenceRemoved(Sequence sequence) {
        logger.info("Sequence [{}] removed", sequence.getName());
        wrappedStrategy.sequenceRemoved(sequence);
    }

    @Override
    public void sequenceUpdated(Sequence sourceSequence, Sequence targetSequence) {
        logger.info("Sequence [{}] updated", targetSequence.getName());
        wrappedStrategy.sequenceUpdated(sourceSequence, targetSequence);
    }
}

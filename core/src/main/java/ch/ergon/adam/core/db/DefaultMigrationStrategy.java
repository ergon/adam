package ch.ergon.adam.core.db;

import ch.ergon.adam.core.db.interfaces.MigrationStrategy;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.core.helper.Pair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public class DefaultMigrationStrategy implements MigrationStrategy {

    Set<Table> tablesToCreate = new LinkedHashSet<>();
    Set<Table> tablesToDrop = new LinkedHashSet<>();
    Set<Pair<Table, Table>> tablesToRecreate = new LinkedHashSet<>();
    Set<Pair<Table, Table>> tablesToRename = new LinkedHashSet<>();
    Set<Index> indexesToCreate = new LinkedHashSet<>();
    Set<Index> indexesToDrop = new LinkedHashSet<>();
    Set<ForeignKey> foreignKeysToCreate = new LinkedHashSet<>();
    Set<ForeignKey> foreignKeysToDrop = new LinkedHashSet<>();
    Set<Field> fieldsToDrop = new LinkedHashSet<>();
    Set<Field> fieldsToAdd = new LinkedHashSet<>();
    Set<Field> fieldsToChangeDefault = new LinkedHashSet<>();
    Set<View> viewsToCreate = new LinkedHashSet<>();
    Set<View> viewsToDrop = new LinkedHashSet<>();
    Set<DbEnum> enumsToCreate = new LinkedHashSet<>();
    Set<DbEnum> enumsToUpdate = new LinkedHashSet<>();
    Set<Field> tableFieldsToChangeTypeForEnumMigration = new LinkedHashSet<>();
    Set<DbEnum> enumsToDrop = new LinkedHashSet<>();
    Set<Constraint> constraintsToDrop = new LinkedHashSet<>();
    Set<Constraint> constraintsToCreate = new LinkedHashSet<>();
    Set<Sequence> sequencesToCreate = new LinkedHashSet<>();
    Set<Sequence> sequencesToDrop = new LinkedHashSet<>();

    private Schema sourceSchema;
    private Schema targetSchema;

    @Override
    public void tableAdded(Table newTable) {
        tablesToCreate.add(newTable);
        indexesToCreate.addAll(newTable.getIndexes());
        foreignKeysToCreate.addAll(newTable.getForeignKeys());
        constraintsToCreate.addAll(newTable.getConstraints());
    }

    @Override
    public void tableRenamed(Table oldTable, Table newTable) {
        tablesToRename.add(new Pair<>(oldTable, newTable));
    }

    @Override
    public void tableRemoved(Table oldTable) {
        tablesToDrop.stream().flatMap(table -> table.getFields().stream()).flatMap(field -> field.getReferencingIndexes().stream()).forEach(index -> {
            foreignKeysToDrop.addAll(index.getReferencingForeignKeys());
        });
        tablesToDrop.add(oldTable);
    }

    @Override
    public void fieldAdded(Field newField) {
        Table sourceTable = getPreviousTable(newField.getTable());
        if (newField.getIndex() < sourceTable.getFields().size()) {
            recreateTable(sourceTable, newField.getTable());
        } else if (!newField.isNullable() && newField.getDefaultValue() == null) {
            //Add not null field without default
            recreateTable(sourceTable, newField.getTable());
        } else {
            fieldsToAdd.add(newField);
        }
    }

    private Table getPreviousTable(Table newTable) {
        Table previousTable = sourceSchema.getTable(newTable.getName());
        if (previousTable == null && newTable.getPreviousName() != null) {
            previousTable = sourceSchema.getTable(newTable.getPreviousName());
        }
        return previousTable;
    }

    private void recreateTable(Table sourceTable, Table targetTable) {
        // TODO: try to prevent using this function since it causes an expensive migration.
        sourceTable.getIndexes().forEach(this::dropIndex);
        foreignKeysToDrop.addAll(sourceTable.getForeignKeys());
        for (Field newField : targetTable.getFields()) {
            newField.getReferencingIndexes().forEach(newIndex -> {
                indexesToCreate.add(newIndex);
                foreignKeysToCreate.addAll(newIndex.getReferencingForeignKeys());
            });
        }
        constraintsToCreate.addAll(targetTable.getConstraints());
        foreignKeysToCreate.addAll(targetTable.getForeignKeys());
        tablesToRecreate.add(new Pair<>(sourceTable, targetTable));
    }

    @Override
    public void fieldRemoved(Field oldField) {
        dropField(oldField);
    }

    private void dropField(Field oldField) {
        fieldsToDrop.add(oldField);
        oldField.getReferencingIndexes().forEach(this::dropIndex);
    }

    @Override
    public void fieldRenamed(Field oldField, Field newField) {
        recreateTable(oldField.getTable(), newField.getTable());
    }

    @Override
    public void fieldIndexChange(Field oldField, Field newField) {
        recreateTable(oldField.getTable(), newField.getTable());
    }

    @Override
    public void fieldDefaultChanged(Field oldField, Field newField) {
        fieldsToChangeDefault.add(newField);
    }


    @Override
    public void fileTypeChanged(Field oldField, Field newField) {
        recreateTable(oldField.getTable(), newField.getTable());
    }

    @Override
    public void indexAdded(Index newIndex) {
        indexesToCreate.add(newIndex);
    }

    @Override
    public void indexUpdated(Index oldIndex, Index newIndex) {
        indexesToCreate.add(newIndex);
        dropIndex(oldIndex);
        foreignKeysToCreate.addAll(newIndex.getReferencingForeignKeys());
    }

    private void dropIndex(Index oldIndex) {
        foreignKeysToDrop.addAll(oldIndex.getReferencingForeignKeys());
        if (oldIndex.isPrimary()) {
            constraintsToDrop.addAll(
                    oldIndex.getTable().getConstraints().stream()
                            .filter(constraint -> constraint instanceof PrimaryKeyConstraint)
                            .collect(toList()));
        } else {
            indexesToDrop.add(oldIndex);
        }
    }

    @Override
    public void indexRemoved(Index oldIndex) {
        dropIndex(oldIndex);
    }

    @Override
    public void foreignKeyAdded(ForeignKey newForeignKey) {
        foreignKeysToCreate.add(newForeignKey);
    }

    @Override
    public void foreignKeyUpdated(ForeignKey oldForeignKey, ForeignKey newForeignKey) {
        foreignKeysToCreate.add(newForeignKey);
        foreignKeysToDrop.add(oldForeignKey);
    }

    @Override
    public void foreignKeyRemoved(ForeignKey oldForeignKey) {
        foreignKeysToDrop.add(oldForeignKey);
    }

    @Override
    public void viewAdded(View newView) {
        viewsToCreate.add(newView);
    }

    @Override
    public void viewRemoved(View oldView) {
        viewsToDrop.add(oldView);
    }

    @Override
    public void viewUpdated(View oldView, View newView) {
        viewRemoved(oldView);
        viewAdded(newView);
    }

    @Override
    public void setSourceSchema(Schema sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    @Override
    public void setTargetSchema(Schema targetSchema) {
        this.targetSchema = targetSchema;
    }


    @Override
    public void enumAdded(DbEnum newEnum) {
        enumsToCreate.add(newEnum);

    }

    @Override
    public void enumRemoved(DbEnum oldEnum) {
        enumsToDrop.add(oldEnum);
    }

    @Override
    public void enumUpdated(DbEnum oldEnum, DbEnum newEnum) {
        enumsToUpdate.add(oldEnum);
        enumsToCreate.add(newEnum);
        List<Field> referencingTableFields = oldEnum.getReferencingFields().stream()
            .filter(f -> f.getContainer() instanceof Table)
            .collect(toList());
        tableFieldsToChangeTypeForEnumMigration.addAll(referencingTableFields);
    }

    @Override
    public void constraintAdded(Constraint newConstraint) {
        if (newConstraint instanceof PrimaryKeyConstraint) {
            //Primary keys are handled with their index
            return;
        }
        constraintsToCreate.add(newConstraint);
    }

    @Override
    public void constraintRemoved(Constraint oldConstraint) {
        if (oldConstraint instanceof PrimaryKeyConstraint) {
            //Primary keys are handled with their index
            return;
        }
        constraintsToDrop.add(oldConstraint);
    }

    @Override
    public void constraintUpdated(Constraint oldConstraint, Constraint newConstraint) {
        constraintRemoved(oldConstraint);
        constraintAdded(newConstraint);
    }

    @Override
    public void sequenceAdded(Sequence sequence) {
        sequencesToCreate.add(sequence);
    }

    @Override
    public void sequenceRemoved(Sequence sequence) {
        sequencesToDrop.add(sequence);
    }

    @Override
    public void sequenceUpdated(Sequence sourceSequence, Sequence targetSequence) {
        sequencesToDrop.add(sourceSequence);
        sequencesToCreate.add(targetSequence);
    }

    @Override
    public void apply(SchemaSink sink) {
        fixupSinkSpecifics(sink);

        cleanupForRecreatedTables();

        recreateDependentViews();

        viewsToDrop.stream().sorted(comparing(this::maxDependencyDepth)).forEach(sink::dropView);

        foreignKeysToDrop.forEach(sink::dropForeignKey);

        constraintsToDrop.forEach(sink::dropConstraint);

        indexesToDrop.forEach(sink::dropIndex);

        sequencesToDrop.forEach(sink::dropSequence);

        tableFieldsToChangeTypeForEnumMigration.forEach(field -> {
            sink.dropDefault(field);
            sink.changeFieldType(field, field, DataType.CLOB);
        });

        enumsToUpdate.forEach(sink::dropEnum);

        enumsToCreate.forEach(sink::createEnum);

        tableFieldsToChangeTypeForEnumMigration.forEach(field -> {
            sink.changeFieldType(field, field, field.getDataType());
            sink.setDefault(field);
        });

        tablesToRename.forEach(pair -> sink.renameTable(pair.getFirst(), pair.getSecond().getName()));

        fieldsToAdd.forEach(sink::addField);

        tablesToCreate.forEach(sink::createTable);

        tablesToRecreate.forEach(pair -> applyTableRecreate(pair.getFirst(), pair.getSecond(), sink));

        fieldsToDrop.stream().forEach(fieldToDrop -> sink.dropField(fieldToDrop, getNewTableForOldField(fieldToDrop)));

        tablesToDrop.forEach(sink::dropTable);

        enumsToDrop.forEach(sink::dropEnum);

        sequencesToCreate.forEach(sink::createSequence);

        fieldsToChangeDefault.forEach(sink::setDefault);

        indexesToCreate.forEach(sink::createIndex);

        constraintsToCreate.forEach(sink::createConstraint);

        foreignKeysToCreate.forEach(sink::createForeignKey);

        viewsToCreate.stream().sorted(comparing(this::minDependencyDepth)).forEach(sink::createView);
    }

    private void fixupSinkSpecifics(SchemaSink sink) {
        if (!sink.supportAlterAndDropField()) {
            for (Field field : this.fieldsToDrop) {
                Table newTable = getNewTableForOldField(field);
                recreateTable(field.getTable(), newTable);
            }
            for (Field field : this.fieldsToChangeDefault) {
                Table newTable = getNewTableForOldField(field);
                recreateTable(field.getTable(), newTable);
            }
            this.fieldsToDrop.clear();
            this.fieldsToChangeDefault.clear();
        }
    }

    private Table getNewTableForOldField(Field field) {
        String oldTableName = field.getTable().getName();
        Table newTable = tablesToRename.stream()
            .filter(pair -> pair.getFirst().getName().equals(oldTableName))
            .map(pair -> pair.getSecond())
            .findFirst()
            .orElse(targetSchema.getTable(oldTableName));
        return newTable;
    }

    private int minDependencyDepth(Relation relation) {
        return -maxDependencyDepth(relation);
    }

    private int maxDependencyDepth(Relation relation) {
        if (relation instanceof Table) {
            return 0;
        }
        return ((View) relation).getBaseRelations().stream()
                .map(this::maxDependencyDepth)
                .min(Integer::compareTo).orElse(0) - 1;

    }

    private void recreateDependentViews() {
        concat(tablesToRecreate.stream().map(Pair::getFirst), viewsToDrop.stream())
                .distinct()
                .forEach(this::recreateDependentView);
    }

    private void recreateDependentView(Relation relation) {
        relation.getDependentViews().forEach(view -> {
            if (viewsToCreate.stream().noneMatch(newView -> newView.getName().equals(view.getName()))) {
                viewsToDrop.add(view);
                View targetView = targetSchema.getView(view.getName());
                if (targetView != null) {
                    viewsToCreate.add(targetView);
                }
            }
            recreateDependentView(view);
        });
    }

    private void cleanupForRecreatedTables() {
        // Since we recreate the table, no other migrations are required on that table or its fields
        for (Pair<Table, Table> recreatePair : tablesToRecreate) {
            tablesToRename.removeIf(pair -> pair.getSecond().equals(recreatePair.getSecond()));
            fieldsToAdd.removeIf(field -> field.getTable() == recreatePair.getSecond());
            fieldsToDrop.removeIf(field -> field.getTable() == recreatePair.getFirst());
            fieldsToChangeDefault.removeIf(field -> field.getTable() == recreatePair.getSecond());
        }
    }

    private void applyTableRecreate(Table sourceTable, Table targetTable, SchemaSink sink) {
        String insertSourceTableName;
        if (sourceTable.getName().equals(targetTable.getName())) {
            insertSourceTableName = "tmp_" + sourceTable.getName();
            sink.dropSequencesAndDefaults(sourceTable);
            sink.renameTable(sourceTable, insertSourceTableName);
        } else {
            insertSourceTableName = sourceTable.getName();
        }

        sink.createTable(targetTable);

        sink.copyData(sourceTable, targetTable, insertSourceTableName);

        sink.adjustSequences(targetTable);

        sink.dropTable(new Table(insertSourceTableName));
    }
}

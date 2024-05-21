package ch.ergon.adam.core.db;

import ch.ergon.adam.core.db.interfaces.MigrationStrategy;
import ch.ergon.adam.core.db.schema.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class SchemaDiffExtractor {

    private final static List<DataType> DATA_TYPES_WITH_IGNORED_FIELD_SIZE =
        asList(DataType.TIMESTAMPWITHTIMEZONE, DataType.LOCALDATE, DataType.LOCALDATETIME, DataType.LOCALTIME, DataType.OFFSETDATETIME, DataType.OFFSETTIME, DataType.OFFSETTIME, DataType.TIMESTAMP);

    private final Schema target;
    private final Schema source;

    public SchemaDiffExtractor(Schema source, Schema target) {
        this.source = source;
        this.target = target;
    }

    private <T extends SchemaItem> void addedRemovedUpdated(
        Collection<T> sourceList,
        Collection<T> targetList,
        Consumer<T> addedHandler,
        Consumer<T> removedHandler,
        BiConsumer<T,T> processHandler) {
        addedRemovedUpdated(sourceList, targetList, targetItem -> null, addedHandler, removedHandler, processHandler);
    }

    // This helper function calls the added, removed or updated handler for each item in the list.
    private <T extends SchemaItem> void addedRemovedUpdated(
        Collection<T> sourceList,
        Collection<T> targetList,
        Function<T, String> previousObjectNameFunction,
        Consumer<T> addedHandler,
        Consumer<T> removedHandler,
        BiConsumer<T, T> processHandler) {

        Map<String, T> sourceMap = sourceList.stream().collect(toLinkedMap(T::getName, identity()));
        Map<String, T> targetMap = targetList.stream().collect(toLinkedMap(T::getName, identity()));

        Map<String, T> previousNameToTargetItem = targetList.stream()
            .filter(targetItem -> !sourceMap.containsKey(targetItem.getName()))
            .filter(targetItem -> !targetMap.containsKey(previousObjectNameFunction.apply(targetItem)))
            .filter(targetItem -> sourceMap.containsKey(previousObjectNameFunction.apply(targetItem)))
            .collect(toLinkedMap(previousObjectNameFunction::apply, identity()));

        // Removed
        sourceList.stream()
            .filter(sourceItem -> !targetMap.containsKey(sourceItem.getName()))
            .filter(sourceItem -> !previousNameToTargetItem.containsKey(sourceItem.getName()))
            .forEach(removedHandler::accept);

        // Added
        targetList.stream()
            .filter(targetItem -> !sourceMap.containsKey(targetItem.getName()))
            .filter(targetItem -> !previousNameToTargetItem.containsValue(targetItem))
            .forEach(addedHandler::accept);

        // Process
        targetList.stream()
            .filter(item -> sourceMap.get(item.getName()) != null)
            .forEach(targetItem -> {
                T sourceItem = sourceMap.get(targetItem.getName());
                processHandler.accept(sourceItem, targetItem);
            });

        // Renamed
        previousNameToTargetItem.values().forEach(targetItem -> {
            T sourceItem = sourceMap.get(previousObjectNameFunction.apply(targetItem));
            processHandler.accept(sourceItem, targetItem);
        });

    }

    public void process(MigrationStrategy strategy) {
        addedRemovedUpdated(
            source.getTables(),
            target.getTables(),
            Table::getPreviousName,
            strategy::tableAdded,
            strategy::tableRemoved,
            (sourceTable, targetTable) -> processTable(sourceTable, targetTable, strategy));

        addedRemovedUpdated(
            source.getViews(),
            target.getViews(),
            strategy::viewAdded,
            strategy::viewRemoved,
            (sourceView, targetView) -> processView(sourceView, targetView, strategy));

        addedRemovedUpdated(
            source.getEnums(),
            target.getEnums(),
            strategy::enumAdded,
            strategy::enumRemoved,
            (sourceEnum, targetEnum) -> processEnum(sourceEnum, targetEnum, strategy));

        addedRemovedUpdated(
            source.getSequences(),
            target.getSequences(),
            strategy::sequenceAdded,
            strategy::sequenceRemoved,
            (sourceSequence, targetSequence) -> processSequence(sourceSequence, targetSequence, strategy));

    }

    private void processSequence(Sequence sourceSequence, Sequence targetSequence, MigrationStrategy strategy) {
        if ((targetSequence.getIncrement() != null && !targetSequence.getIncrement().equals(sourceSequence.getIncrement()))
            || (targetSequence.getMaxValue() != null && !targetSequence.getMaxValue().equals(sourceSequence.getMaxValue()))
            || (targetSequence.getMinValue() != null && !targetSequence.getMinValue().equals(sourceSequence.getMinValue()))
            || (targetSequence.getStartValue() != null && !targetSequence.getStartValue().equals(sourceSequence.getStartValue()))) {
            strategy.sequenceUpdated(sourceSequence, targetSequence);
        }
    }

    private void processView(View sourceView, View targetView, MigrationStrategy strategy) {
        if (!Objects.equals(sourceView.getViewDefinition(), targetView.getViewDefinition())) {
            strategy.viewUpdated(sourceView, targetView);
        }
    }

    private void processEnum(DbEnum sourceEnum, DbEnum targetEnum, MigrationStrategy strategy) {
        if (!Arrays.equals(sourceEnum.getValues(), targetEnum.getValues())) {
            strategy.enumUpdated(sourceEnum, targetEnum);
        }
    }

    private void processTable(Table sourceTable, Table targetTable, MigrationStrategy strategy) {
        if (!Objects.equals(sourceTable.getName(), targetTable.getName())) {
            strategy.tableRenamed(sourceTable, targetTable);
        }

        // Process fields
        addedRemovedUpdated(
            sourceTable.getFields(),
            targetTable.getFields(),
            Field::getSqlForNew,
            strategy::fieldAdded,
            strategy::fieldRemoved,
            (sourceField, targetField) -> processField(sourceField, targetField, strategy));

        // Process indexes
        addedRemovedUpdated(
            sourceTable.getIndexes(),
            targetTable.getIndexes(),
            strategy::indexAdded,
            strategy::indexRemoved,
            (sourceIndex, targetIndex) -> processIndexes(sourceIndex, targetIndex, strategy));

        // Process foreign keys
        addedRemovedUpdated(
            sourceTable.getForeignKeys(),
            targetTable.getForeignKeys(),
            strategy::foreignKeyAdded,
            strategy::foreignKeyRemoved,
            (sourceFK, targetFK) -> processForeignKey(sourceFK, targetFK, strategy));

        // Process constraints keys
        List<Constraint> sourceConstraints = sourceTable.getConstraints().stream()
            .filter(c -> !(c instanceof PrimaryKeyConstraint))
            .collect(toList());
        List<Constraint> targetConstraints = targetTable.getConstraints().stream()
            .filter(c -> !(c instanceof PrimaryKeyConstraint))
            .collect(toList());

        addedRemovedUpdated(
            sourceConstraints,
            targetConstraints,
            strategy::constraintAdded,
            strategy::constraintRemoved,
            (sourceConstraint, targetConstraint) -> processConstraint(sourceConstraint, targetConstraint, strategy));
    }

    private void processConstraint(Constraint sourceConstraint, Constraint targetConstraint, MigrationStrategy strategy) {
        if (sourceConstraint instanceof RuleConstraint) {
            RuleConstraint sourceRuleConstraint = (RuleConstraint) sourceConstraint;
            RuleConstraint targetRuleConstraint = (RuleConstraint) targetConstraint;
            if (!Objects.equals(sourceRuleConstraint.getRule(), targetRuleConstraint.getRule())) {
                strategy.constraintUpdated(sourceConstraint, targetConstraint);
            }
        }
    }

    private void processField(Field sourceField, Field targetField, MigrationStrategy strategy) {
        if (!Objects.equals(sourceField.getName(), targetField.getName())) {
            strategy.fieldRenamed(sourceField, targetField);
        }

        if (!Objects.equals(sourceField.getDefaultValue(), targetField.getDefaultValue())) {
            strategy.fieldDefaultChanged(sourceField, targetField);
        }

        if (sourceField.getIndex() != targetField.getIndex()) {
            strategy.fieldIndexChange(sourceField, targetField);
        }

        boolean sameEnum = sourceField.getDbEnum() == null && targetField.getDbEnum() == null ||
            sourceField.getDbEnum() != null && targetField.getDbEnum() != null
                && Objects.equals(sourceField.getDbEnum().getName(), targetField.getDbEnum().getName());

        boolean fieldSizeChange = !ignoreFieldSizeChange(targetField.getDataType()) && (
            !Objects.equals(sourceField.getLength(), targetField.getLength())
                || !Objects.equals(sourceField.getPrecision(), targetField.getPrecision())
                || !Objects.equals(sourceField.getScale(), targetField.getScale())
        );

        if (!Objects.equals(sourceField.getDataType(), targetField.getDataType())
            || !sameEnum
            || fieldSizeChange
            || sourceField.isSequence() != targetField.isSequence()
            || sourceField.isNullable() != targetField.isNullable()
            || sourceField.isArray() != targetField.isArray()) {
            strategy.fileTypeChanged(sourceField, targetField);
        }

    }

    private boolean ignoreFieldSizeChange(DataType dataType) {
        return DATA_TYPES_WITH_IGNORED_FIELD_SIZE.contains(dataType);
    }

    private void processIndexes(Index sourceIndex, Index targetIndex, MigrationStrategy strategy) {
        if (sourceIndex.isUnique() != targetIndex.isUnique()
            || sourceIndex.isPrimary() != targetIndex.isPrimary()
            || !Arrays.equals(createSchemaItemNameArray(sourceIndex.getFields()), createSchemaItemNameArray(targetIndex.getFields()))
            || !Objects.equals(sourceIndex.getWhere(), targetIndex.getWhere())) {
            strategy.indexUpdated(sourceIndex, targetIndex);
        }
    }

    private void processForeignKey(ForeignKey sourceFK, ForeignKey targetFK, MigrationStrategy strategy) {
        if (!sourceFK.getField().getName().equals(targetFK.getField().getName())) {
            strategy.foreignKeyUpdated(sourceFK, targetFK);
        }
    }
}

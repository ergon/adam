package ch.ergon.adam.core.db.schema;

import ch.ergon.adam.core.helper.CollectorsHelper;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class Table extends Relation {

    private Map<String, Index> indexes = new LinkedHashMap<>();
    private Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>();
    private Map<String, Constraint> constraints = new LinkedHashMap<>();
    private String previousName;

    public Table(String name) {
        super(name);
    }

    @Nullable
    public Index getIndex(String name) {
        return indexes.get(name);
    }

    public Collection<Index> getIndexes() {
        return indexes.values();
    }

    public void setIndexes(List<Index> indexes) {
        setTable(indexes);
        this.indexes = indexes.stream().collect(CollectorsHelper.toLinkedMap(Index::getName, identity()));
    }

    public Collection<ForeignKey> getForeignKeys() {
        return foreignKeys.values();
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        setTable(foreignKeys);
        this.foreignKeys = foreignKeys.stream().collect(CollectorsHelper.toLinkedMap(ForeignKey::getName, identity()));
    }

    @Nullable
    public Constraint getConstraint(String name) {
        return constraints.get(name);
    }

    public Collection<Constraint> getConstraints() {
        return constraints.values();
    }

    public void setConstraints(List<Constraint> constraints) {
        setTable(constraints);
        this.constraints = constraints.stream().collect(CollectorsHelper.toLinkedMap(Constraint::getName, identity()));
    }

    private <T extends  TableItem> void setTable(Collection<T> items) {
        items.forEach(item -> item.setTable(this));
    }

    public Index getIndex(List<Field> foreignFields) {
        String[] foreignFieldNames = CollectorsHelper.createSchemaItemNameArray(foreignFields);
        List<Index> matchingIndexes = indexes.values().stream()
            .filter(index -> Arrays.equals(foreignFieldNames, CollectorsHelper.createSchemaItemNameArray(index.getFields())))
            .collect(toList());
        if (matchingIndexes.isEmpty()) {
            throw new RuntimeException("No index with matching field order " + String.join(",", foreignFieldNames) + " found.");
        }
        return matchingIndexes.get(0);

    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }
}

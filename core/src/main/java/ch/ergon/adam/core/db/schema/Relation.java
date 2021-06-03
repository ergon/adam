package ch.ergon.adam.core.db.schema;

import ch.ergon.adam.core.helper.CollectorsHelper;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.function.Function.identity;

public abstract class Relation extends SchemaItem {

    private Map<String, Field> fields = new LinkedHashMap<>();
    private Map<String, Integer> fieldNameToIndex = new LinkedHashMap<>();
    private List<View> dependentViews = new LinkedList<>();

    public Relation(String name) {
        super(name);
    }

    @Nullable
    public Field getField(String name) {
        return fields.get(name);
    }

    public int getFieldIndex(Field field) {
        return fieldNameToIndex.get(field.getName());
    }

    public Collection<Field> getFields() {
        return fields.values();
    }

    public void setFields(Collection<Field> fields) {
        fields.forEach(field -> field.setContainer(this));
        this.fields = fields.stream().collect(CollectorsHelper.toLinkedMap(Field::getName, identity()));
        this.fieldNameToIndex = new HashMap<>(fields.size());
        fields.stream().map(Field::getName).forEach(name -> fieldNameToIndex.put(name, fieldNameToIndex.size()));
    }

    public Collection<View> getDependentViews() {
        return dependentViews;
    }

    public void addDependentView(View view) {
        dependentViews.add(view);
    }
}

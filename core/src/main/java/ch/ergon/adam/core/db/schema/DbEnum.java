package ch.ergon.adam.core.db.schema;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DbEnum extends SchemaItem {

    private String[] values;
    private List<Field> referencingFields = new LinkedList<>();

    public DbEnum(String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }


    public Collection<Field> getReferencingFields() {
        return referencingFields;
    }

    public void addReferencingField(Field field) {
        referencingFields.add(field);
    }
}

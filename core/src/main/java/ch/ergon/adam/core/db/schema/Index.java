package ch.ergon.adam.core.db.schema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Index extends TableItem {

    private boolean isPrimary;
    private boolean isUnique;
    private List<Field> fields;
    private String where;
    private final Set<ForeignKey> referencingForeignKeys = new LinkedHashSet<>();

    public Index(String name) {
        super(name);
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        fields.forEach(field -> field.addReferencingIndex(this));
        this.fields = fields;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void addReferencingForeignKey(ForeignKey foreignKey) {
        referencingForeignKeys.add(foreignKey);
    }

    public Collection<ForeignKey> getReferencingForeignKeys() {
        return referencingForeignKeys;
    }
}

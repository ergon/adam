package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlIndex extends YmlSchemaItem {

    private boolean isPrimary;
    private boolean isUnique;
    private String where;
    private String[] fields = new String[0];

    public YmlIndex(@JsonProperty("name")String name) {
        super(name);
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
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

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }
}

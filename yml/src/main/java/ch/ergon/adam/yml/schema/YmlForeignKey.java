package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlForeignKey extends YmlSchemaItem {

    private String field;
    private String targetIndex;
    private String targetTable;

    public YmlForeignKey(@JsonProperty("name")String name) {
        super(name);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(String targetIndex) {
        this.targetIndex = targetIndex;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }
}

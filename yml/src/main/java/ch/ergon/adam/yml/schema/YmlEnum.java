package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlEnum extends YmlSchemaItem {

    private String[] values = new String[0];

    public YmlEnum(@JsonProperty("name")String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }
}

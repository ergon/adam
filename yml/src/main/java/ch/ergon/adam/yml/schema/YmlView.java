package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlView extends YmlSchemaItem {

    private String viewDefinition;

    private String[] dependencies = new String[0];

    public YmlView(@JsonProperty("name")String name) {
        super(name);
    }

    public String getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(String viewDefinition) {
        this.viewDefinition = viewDefinition;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
}

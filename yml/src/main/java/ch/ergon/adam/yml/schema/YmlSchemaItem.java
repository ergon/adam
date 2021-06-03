package ch.ergon.adam.yml.schema;

public abstract class YmlSchemaItem {

    private final String name;

    protected YmlSchemaItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package ch.ergon.adam.core.db.schema;

public abstract class SchemaItem {

    private final String name;

    protected SchemaItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

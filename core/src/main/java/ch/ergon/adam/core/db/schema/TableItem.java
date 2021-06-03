package ch.ergon.adam.core.db.schema;

public abstract class TableItem extends SchemaItem {

    private Table table;

    protected TableItem(String name) {
        super(name);
    }

    public Table getTable() {
        return table;
    }

    protected void setTable(Table table) {
        this.table = table;
    }
}

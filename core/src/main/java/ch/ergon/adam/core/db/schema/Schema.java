package ch.ergon.adam.core.db.schema;

import ch.ergon.adam.core.helper.CollectorsHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.function.Function.identity;

public class Schema {

    private Map<String, Table> tables = new LinkedHashMap<>();
    private Map<String, View> views = new LinkedHashMap<>();
    private Map<String, DbEnum> enums = new LinkedHashMap<>();
    private Map<String, Sequence> sequences = new LinkedHashMap<>();

    public Table getTable(String name) {
        return tables.get(name);
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    public void setTables(Collection<Table> tables) {
        this.tables = tables.stream().collect(CollectorsHelper.toLinkedMap(SchemaItem::getName, identity()));
    }

    public View getView(String name) {
        return views.get(name);
    }

    public Collection<View> getViews() {
        return views.values();
    }

    public void setViews(Collection<View> views) {
        this.views = views.stream().collect(CollectorsHelper.toLinkedMap(SchemaItem::getName, identity()));
    }

    @Nullable
    public DbEnum getEnum(String name) {
        return enums.get(name);
    }

    public Collection<DbEnum> getEnums() {
        return enums.values();
    }

    public void setEnums(Collection<DbEnum> enums) {
        this.enums = enums.stream().collect(CollectorsHelper.toLinkedMap(SchemaItem::getName, identity()));
    }

    public Sequence getSequence(String name) {
        return sequences.get(name);
    }

    public Collection<Sequence> getSequences() {
        return sequences.values();
    }

    public void setSequences(Collection<Sequence> sequences) {
        this.sequences = sequences.stream().collect(CollectorsHelper.toLinkedMap(Sequence::getName, identity()));
    }

    public Relation getRelation(String name) {
        if (views.containsKey(name)) {
            return views.get(name);
        }
        return tables.get(name);
    }
}

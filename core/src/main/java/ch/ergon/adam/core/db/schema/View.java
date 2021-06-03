package ch.ergon.adam.core.db.schema;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class View extends Relation {

    private String viewDefinition;
    private List<Relation> baseRelations = new LinkedList<>();

    public View(String name) {
        super(name);
    }

    public String getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(String viewDefinition) {
        this.viewDefinition = viewDefinition;
    }


    public Collection<Relation> getBaseRelations() {
        return baseRelations;
    }

    public void addBaseRelation(Relation base) {
        baseRelations.add(base);
        base.addDependentView(this);
    }
}

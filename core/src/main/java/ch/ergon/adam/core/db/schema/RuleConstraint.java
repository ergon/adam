package ch.ergon.adam.core.db.schema;

public class RuleConstraint extends Constraint {

    private String rule;

    public RuleConstraint(String name) {
        super(name);
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}

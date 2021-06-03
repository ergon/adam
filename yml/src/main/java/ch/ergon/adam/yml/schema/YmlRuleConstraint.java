package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlRuleConstraint extends YmlSchemaItem {

    private String rule;

    public YmlRuleConstraint(@JsonProperty("name")String name) {
        super(name);
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}

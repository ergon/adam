package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@JsonIgnoreProperties({"$id", "$schema"})
public class YmlTable extends YmlSchemaItem {

    private List<YmlField> fields = newArrayList();
    private List<YmlIndex> indexes = newArrayList();
    private List<YmlForeignKey> foreignKeys = newArrayList();
    private List<YmlRuleConstraint> ruleConstraints = newArrayList();
    private String previousName;

    public YmlTable(@JsonProperty("name")String name) {
        super(name);
    }

    public List<YmlField> getFields() {
        return fields;
    }

    public void setFields(List<YmlField> fields) {
        this.fields = fields;
    }

    public List<YmlIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<YmlIndex> indexes) {
        this.indexes = indexes;
    }

    public List<YmlForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<YmlForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public List<YmlRuleConstraint> getRuleConstraints() {
        return ruleConstraints;
    }

    public void setRuleConstraints(List<YmlRuleConstraint> ruleConstraints) {
        this.ruleConstraints = ruleConstraints;
    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }
}

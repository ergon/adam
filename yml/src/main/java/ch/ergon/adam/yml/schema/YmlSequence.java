package ch.ergon.adam.yml.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YmlSequence extends YmlSchemaItem {

    private Long startValue;

    private Long minValue;

    private Long maxValue;

    private Long increment;

    public YmlSequence(@JsonProperty("name")String name) {
        super(name);
    }

    public Long getIncrement() {
        return increment;
    }

    public void setIncrement(Long increment) {
        this.increment = increment;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public Long getStartValue() {
        return startValue;
    }

    public void setStartValue(Long startValue) {
        this.startValue = startValue;
    }
}

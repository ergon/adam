package ch.ergon.adam.core.db.schema;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Field extends SchemaItem {

    private Relation container;
    private DataType dataType;
    private DbEnum dbEnum;
    private boolean isArray;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String defaultValue;
    private String sqlForNew;
    private boolean nullable;
    private boolean sequence;
    private final Set<Index> referencingIndexes = new LinkedHashSet<>();

    public Field(String name) {
        super(name);
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public int getIndex() {
        return getContainer().getFieldIndex(this);
    }

    public void addReferencingIndex(Index index) {
        referencingIndexes.add(index);
    }

    public Collection<Index> getReferencingIndexes() {
        return referencingIndexes;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setContainer(Relation container) {
        this.container = container;
    }

    public Relation getContainer() {
        return container;
    }

    public Table getTable() {
        return (Table)container;
    }

    public DbEnum getDbEnum() {
        return dbEnum;
    }

    public void setDbEnum(@Nonnull DbEnum dbEnum) {
        this.dbEnum = dbEnum;
        dbEnum.addReferencingField(this);
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isSequence() {
        return sequence;
    }

    public void setSequence(boolean sequence) {
        this.sequence = sequence;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getSqlForNew() {
        return sqlForNew;
    }

    public void setSqlForNew(String sqlForNew) {
        this.sqlForNew = sqlForNew;
    }

    public String getTypeDescription() {
        String typeDescription = this.getDataType().name();
        if (this.getDataType() == DataType.ENUM) {
            typeDescription = getDbEnum().getName();
        }
        if (length != null) {
            typeDescription += "(" + length + ")";
        }
        if (precision != null) {
            typeDescription += "(" + precision;
            if (scale != null) {
                typeDescription += "," + scale;
            }
            typeDescription += ")";
        }
        if (isArray()) {
            typeDescription += "[]";
        }
        if (!isNullable()) {
            typeDescription += " not";
        }
        typeDescription += " null";
        return typeDescription;
    }
}

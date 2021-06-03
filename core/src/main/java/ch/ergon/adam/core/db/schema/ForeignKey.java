package ch.ergon.adam.core.db.schema;

import javax.annotation.Nonnull;

public class ForeignKey extends Constraint {

    private Field field;
    private Index targetIndex;

    public ForeignKey(String name) {
        super(name);
    }

    public Index getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(@Nonnull Index targetIndex) {
        this.targetIndex = targetIndex;
        targetIndex.addReferencingForeignKey(this);
    }

    public Field getField() {
        return field;
    }

    public void setField(@Nonnull Field field) {
        this.field = field;
    }
}

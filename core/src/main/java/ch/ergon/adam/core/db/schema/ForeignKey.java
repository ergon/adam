package ch.ergon.adam.core.db.schema;

import org.jspecify.annotations.NonNull;

public class ForeignKey extends Constraint {

    private Field field;
    private Index targetIndex;

    public ForeignKey(String name) {
        super(name);
    }

    public Index getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(@NonNull Index targetIndex) {
        this.targetIndex = targetIndex;
        targetIndex.addReferencingForeignKey(this);
    }

    public Field getField() {
        return field;
    }

    public void setField(@NonNull Field field) {
        this.field = field;
    }
}

package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.db.schema.Schema;

public class EmptySource extends StubSchemaSource {

    public EmptySource() {
        super(new Schema());
    }
}

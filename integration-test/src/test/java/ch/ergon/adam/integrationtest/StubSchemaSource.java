package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;

public class StubSchemaSource implements SchemaSource {
    private final Schema schema;

    public StubSchemaSource(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void close() {

    }
}

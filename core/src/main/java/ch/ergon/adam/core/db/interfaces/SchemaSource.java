package ch.ergon.adam.core.db.interfaces;

import ch.ergon.adam.core.db.schema.Schema;

public interface SchemaSource extends AutoCloseable {

    Schema getSchema();
}

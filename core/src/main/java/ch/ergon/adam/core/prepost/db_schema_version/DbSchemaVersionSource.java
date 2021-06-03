package ch.ergon.adam.core.prepost.db_schema_version;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.*;

import java.io.IOException;

import static ch.ergon.adam.core.db.schema.DataType.BIGINT;
import static ch.ergon.adam.core.db.schema.DataType.TIMESTAMPWITHTIMEZONE;
import static ch.ergon.adam.core.db.schema.DataType.VARCHAR;
import static com.google.common.collect.Lists.newArrayList;

public class DbSchemaVersionSource implements SchemaSource {

    public static final String SCHEMA_VERSION_TABLE_NAME = "db_schema_version";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema();
        Table table = new Table(SCHEMA_VERSION_TABLE_NAME);
        Field id = createField("id", BIGINT, null, false);
        id.setSequence(true);
        Field sourceVersion = createField("source_version", VARCHAR, 50, true);
        Field targetVersion = createField("target_version", VARCHAR, 50, false);
        Field executionStartedAt = createField("execution_started_at", TIMESTAMPWITHTIMEZONE, null, false);
        Field executionCompletedAt = createField("execution_completed_at", TIMESTAMPWITHTIMEZONE, null, true);
        Field error = createField("error", VARCHAR, 2000, true);
        table.setFields(newArrayList(id, sourceVersion, targetVersion, executionStartedAt, executionCompletedAt, error));
        Index primaryKey = new Index("db_schema_version_pkey");
        primaryKey.setPrimary(true);
        primaryKey.setUnique(true);
        primaryKey.setFields(newArrayList(id));
        table.setIndexes(newArrayList(primaryKey));
        schema.setTables(newArrayList(table));
        return schema;
    }

    private Field createField(String name, DataType type, Integer length, boolean nullable) {
        Field field = new Field(name);
        field.setNullable(nullable);
        field.setDataType(type);
        field.setLength(length);
        return field;

    }

    @Override
    public void close() throws IOException {

    }
}

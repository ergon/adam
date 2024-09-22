package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.SchemaMigrator;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;

public class PostgresSchemaCoverageTest extends AbstractPostgreSQLSchemaCoverageTest {

    @Override
    protected Schema executeTransformation(SchemaSource source) throws Exception {
        try (SchemaSink targetSink = getTargetDbSink()) {
            new SchemaMigrator(getTargetDbSource(), source, targetSink).migrate();
        }
        return getTargetDbSource().getSchema();
    }
}

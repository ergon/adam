package ch.ergon.adam.core.prepost.db_schema_version;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;

public class DbSchemaVersionSourceFactory implements SourceAndSinkAdapter {
    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith("dbschemaversion://");
    }

    @Override
    public SchemaSource createSource(String url) {
        return new DbSchemaVersionSource();
    }

    @Override
    public SchemaSink createSink(String url) {
        throw new UnsupportedOperationException("dbschemaversion does not support sink.");
    }

    @Override
    public SqlExecutor createSqlExecutor(String url) {
        throw new UnsupportedOperationException("dbschemaversion does not support sql executor.");
    }
}

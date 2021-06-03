package ch.ergon.adam.core.db.interfaces;

public interface SourceAndSinkAdapter {

    boolean supportsUrl(String url);

    SchemaSource createSource(String url);

    SchemaSink createSink(String url);

    SqlExecutor createSqlExecutor(String url);
}

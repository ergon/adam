package ch.ergon.adam.sqlite;

import ch.ergon.adam.jooq.JooqSqlExecutor;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;

import java.sql.SQLException;

import static ch.ergon.adam.sqlite.SqliteInMemoryFactory.SQLITE_IN_MEMORY_URL;

public class SqliteFactory implements SourceAndSinkAdapter {

    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith("jdbc:sqlite:") && !url.toLowerCase().startsWith(SQLITE_IN_MEMORY_URL);
    }

    @Override
    public SchemaSource createSource(String url) {
        try {
            return new SqliteSource(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SchemaSink createSink(String url) {
        return new SqliteSink(url);
    }

    @Override
    public SqlExecutor createSqlExecutor(String url) {
        return new JooqSqlExecutor(url, null);
    }
}

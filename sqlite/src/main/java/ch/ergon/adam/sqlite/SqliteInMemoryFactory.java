package ch.ergon.adam.sqlite;

import ch.ergon.adam.jooq.JooqSqlExecutor;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;

public class SqliteInMemoryFactory implements SourceAndSinkAdapter {

    public static String SQLITE_IN_MEMORY_URL = "jdbc:sqlite::memory:";

    private static Map<String, SqliteInMemoryConnectionWrapper> connections = newLinkedHashMap();

    public synchronized static Connection getOrCreateConnection(String url) {
        if (!connections.containsKey(url)) {
            try {
                Connection connection = DriverManager.getConnection(SQLITE_IN_MEMORY_URL);
                SqliteInMemoryConnectionWrapper connectionWrapper = new SqliteInMemoryConnectionWrapper(connection, () -> closeConnection(url));
                connections.put(url, connectionWrapper);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        SqliteInMemoryConnectionWrapper connectionWrapper = connections.get(url);
        connectionWrapper.increaseClientCount();
        return connectionWrapper;
    }

    public static void closeConnection(String url) {
        SqliteInMemoryConnectionWrapper connection = connections.get(url);
        connection.decreaseClientCount();
        if (connection.getClientCount() > 0) {
            return;
        }
        try {
            connections.get(url).getWrappedConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            connections.remove(url);
        }
    }

    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith(SQLITE_IN_MEMORY_URL);
    }

    @Override
    public SchemaSource createSource(String url) {
        return new SqliteSource(getOrCreateConnection(url));
    }

    @Override
    public SchemaSink createSink(String url) {
        return new SqliteSink(getOrCreateConnection(url));
    }

    @Override
    public SqlExecutor createSqlExecutor(String url) {
        return new JooqSqlExecutor(getOrCreateConnection(url), null);
    }
}

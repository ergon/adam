package ch.ergon.adam.postgresql;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;

import java.util.HashMap;
import java.util.Map;

public class PostgreSqlFactory implements SourceAndSinkAdapter {

    private static Map<String, PostgreSqlTransactionWrapper> sqlSinksByUrl = new HashMap<>();

    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith("jdbc:postgresql://");
    }

    @Override
    public SchemaSource createSource(String url) {
        return getTransactionWrapper(url);
    }

    @Override
    public SchemaSink createSink(String url) {
        return getTransactionWrapper(url);
    }

    @Override
    public SqlExecutor createSqlExecutor(String url) {
        return getTransactionWrapper(url);
    }

    public static synchronized PostgreSqlTransactionWrapper getTransactionWrapper(String url) {
        if (!sqlSinksByUrl.containsKey(url) || sqlSinksByUrl.get(url).isClosed()) {
            sqlSinksByUrl.put(url, new PostgreSqlTransactionWrapper(url, extractSchema(url), () -> closeConnection(url)));
        }
        PostgreSqlTransactionWrapper connection = sqlSinksByUrl.get(url);
        connection.increaseClientCount();
        return connection;
    }

    public static void closeConnection(String url) {
        PostgreSqlTransactionWrapper connection = sqlSinksByUrl.get(url);
        connection.decreaseClientCount();
        if (connection.getClientCount() > 0) {
            return;
        }
        sqlSinksByUrl.get(url).reallyClose();
        sqlSinksByUrl.remove(url);
    }

    private static String extractSchema(String url) {
        int idx = url.indexOf("currentSchema=");
        if (idx < 0) {
            return "public";
        }
        idx += "currentSchema=".length();
        int endIdx = url.indexOf("&", idx);
        if (endIdx < 0) {
            return url.substring(idx);
        }
        return url.substring(idx, endIdx);
    }
}

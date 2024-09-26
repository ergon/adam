package ch.ergon.adam.oracle;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;

import java.util.HashMap;
import java.util.Map;

public class OracleSqlFactory implements SourceAndSinkAdapter {

    private static Map<String, OracleSqlTransactionWrapper> sqlSinksByUrl = new HashMap<>();

    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith("jdbc:oracle:");
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

    public static synchronized OracleSqlTransactionWrapper getTransactionWrapper(String url) {
        if (!sqlSinksByUrl.containsKey(url) || sqlSinksByUrl.get(url).isClosed()) {
            sqlSinksByUrl.put(url, new OracleSqlTransactionWrapper(url, extractSchema(url), () -> closeConnection(url)));
        }
        OracleSqlTransactionWrapper connection = sqlSinksByUrl.get(url);
        connection.increaseClientCount();
        return connection;
    }

    public static void closeConnection(String url) {
        OracleSqlTransactionWrapper connection = sqlSinksByUrl.get(url);
        if (connection == null) {
            return;
        }
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

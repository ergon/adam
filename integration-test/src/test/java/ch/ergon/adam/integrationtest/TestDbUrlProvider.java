package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.helper.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TestDbUrlProvider {

    private static Map<String, Pair<Connection, Runnable>> connectionsByUrl = new HashMap<>();

    protected abstract void initDbForTest() throws Exception;

    protected abstract String getSourceDbUrl();

    protected abstract String getTargetDbUrl();

    protected Connection getSourceDbConnection() throws SQLException {
        return getDbConn(getSourceDbUrl());
    }

    protected Connection getTargetDbConnection() throws SQLException {
        return getDbConn(getTargetDbUrl());
    }

    public static void close() {
        connectionsByUrl.keySet().forEach(url -> {
            Pair<Connection, Runnable> connection = connectionsByUrl.get(url);
            try {
                if (!connection.getFirst().isClosed()) {
                    connection.getSecond().run();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        connectionsByUrl.clear();
    }

    private Connection getDbConn(String url) throws SQLException {
        if (!connectionsByUrl.containsKey(url)) {
            connectionsByUrl.put(url, createDbConnection(url));
        }
        return connectionsByUrl.get(url).getFirst();
    }

    protected Pair<Connection, Runnable> createDbConnection(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        Runnable closeHandler = () -> closeDbConnection(connection);
        return new Pair(connection, closeHandler);
    }

    private void closeDbConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}

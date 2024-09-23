package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.helper.Pair;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.postgresql.PostgreSqlFactory;
import ch.ergon.adam.postgresql.PostgreSqlTransactionWrapper;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.String.format;

public class PostgreSqlTestDbUrlProvider extends TestDbUrlProvider {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine");

    private static final String SOURCE_SCHEMA = "test-source";
    protected static final String TARGET_SCHEMA = "test-target";

    @Override
    protected void initDbForTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection(getDbUrl())) {
            conn.createStatement().execute(format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", SOURCE_SCHEMA));
            conn.createStatement().execute(format("CREATE SCHEMA \"%s\"", SOURCE_SCHEMA));
            conn.createStatement().execute(format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", TARGET_SCHEMA));
            conn.createStatement().execute(format("CREATE SCHEMA \"%s\"", TARGET_SCHEMA));
        }
    }

    @Override
    protected String getSourceDbUrl() {
        return getDbUrl(SOURCE_SCHEMA);
    }

    @Override
    protected String getTargetDbUrl() {
        return getDbUrl(TARGET_SCHEMA);
    }

    protected String getDbUrl(String schema) {
        return getDbUrl() + "&currentSchema=" + schema;
    }

    protected String getDbUrl() {
        if (!container.isRunning()) {
            container.start();
        }
        return container.getJdbcUrl() + "&user=" + container.getUsername() + "&password=" + container.getPassword();
    }

    @Override
    protected Pair<Connection, Runnable> createDbConnection(String url) {
        PostgreSqlTransactionWrapper transactionWrapper = PostgreSqlFactory.getTransactionWrapper(url);
        Runnable closeHandler = () -> transactionWrapper.close();
        return new Pair(transactionWrapper.getConnection(), closeHandler);
    }
}

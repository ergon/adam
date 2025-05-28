package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.testcontainers.containers.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.String.format;

public class MariaDbTestDbUrlProvider extends TestDbUrlProvider {
    private static final String SOURCE_SCHEMA = "test-source";
    protected static final String TARGET_SCHEMA = "test-target";

    private static final MariaDBContainer<?> sourceContainer =
        new MariaDBContainer<>("mariadb:11.2")
            .withUsername("root")
            .withDatabaseName(SOURCE_SCHEMA);

    private static final MariaDBContainer<?> targetContainer =
        new MariaDBContainer<>("mariadb:11.2")
            .withUsername("root")
            .withDatabaseName(TARGET_SCHEMA);

    @Override
    protected void initDbForTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection(getDbUrl(sourceContainer))) {
            cleanSchema(conn);
        }
        try (Connection conn = DriverManager.getConnection(getDbUrl(targetContainer))) {
            cleanSchema(conn);
        }
    }

    private void cleanSchema(Connection conn) throws SQLException {
        conn.createStatement().execute(format("DROP SCHEMA IF EXISTS `%s`", SOURCE_SCHEMA));
        conn.createStatement().execute(format("CREATE SCHEMA `%s`", SOURCE_SCHEMA));
        conn.createStatement().execute(format("DROP SCHEMA IF EXISTS `%s`", TARGET_SCHEMA));
        conn.createStatement().execute(format("CREATE SCHEMA `%s`", TARGET_SCHEMA));
    }

    @Override
    protected String getSourceDbUrl() {
        return getDbUrl(sourceContainer);
    }

    @Override
    protected String getTargetDbUrl() {
        return getDbUrl(targetContainer);
    }

    protected String getDbUrl(MariaDBContainer<?> container) {
        if (!container.isRunning()) {
            container.start();
        }
        return container.getJdbcUrl() + "?user=" + container.getUsername() + "&password=" + container.getPassword();
    }
}

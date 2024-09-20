package ch.ergon.adam.integrationtest;

import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.String.format;

public class PostgreSqlToSqliteTestDbUrlProvider extends TestDbUrlProvider {

    private final static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15-alpine");

    private static final String SOURCE_SCHEMA = "test-source";
    protected static final String TARGET_SCHEMA = "test-target";
    private final Path tempFolder;

    public PostgreSqlToSqliteTestDbUrlProvider() throws IOException {
        tempFolder = Files.createTempDirectory("sqlittest");
        tempFolder.toFile().deleteOnExit();
        Files.deleteIfExists(tempFolder.resolve("target.sqlite"));
    }

    @Override
    protected void initDbForTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection(getPostgreSqlDbUrl())) {
            conn.createStatement().execute(format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", SOURCE_SCHEMA));
            conn.createStatement().execute(format("CREATE SCHEMA \"%s\"", SOURCE_SCHEMA));
            conn.createStatement().execute(format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", TARGET_SCHEMA));
            conn.createStatement().execute(format("CREATE SCHEMA \"%s\"", TARGET_SCHEMA));
        }
    }

    @Override
    protected String getSourceDbUrl() {
        return getPostgreSqlDbUrl(SOURCE_SCHEMA);
    }

    @Override
    protected String getTargetDbUrl() {
        return getSqliteDbUrl("target.sqlite");
    }

    protected String getSqliteDbUrl(String dbFileName) {
        return "jdbc:sqlite:" + tempFolder.resolve(dbFileName).toAbsolutePath().toString();
    }

    protected String getPostgreSqlDbUrl(String schema) {
        return getPostgreSqlDbUrl() + "&currentSchema=" + schema;
    }

    private String getPostgreSqlDbUrl() {
        if (!container.isRunning()) {
            container.start();
        }
        return container.getJdbcUrl() + "&user=" + container.getUsername() + "&password=" + container.getPassword();
    }

}

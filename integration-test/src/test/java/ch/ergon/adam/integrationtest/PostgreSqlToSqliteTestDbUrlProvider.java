package ch.ergon.adam.integrationtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class PostgreSqlToSqliteTestDbUrlProvider extends TestDbUrlProvider {

    private static final String DATABASE_URL_PROPERTY = "postgresql_database_url";
    private static final String DATABASE_URL_DEFAULT = "jdbc:postgresql://localhost:5432/test?user=test&password=test";
    private static final String SOURCE_SCHEMA = "test-source";
    protected static final String TARGET_SCHEMA = "test-target";
    private static Map<String, Connection> connectionsByUrl = new HashMap<>();
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
        return System.getProperty(DATABASE_URL_PROPERTY, DATABASE_URL_DEFAULT);
    }

}

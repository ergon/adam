package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.TestDbUrlProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SqliteTestFileDbUrlProvider extends TestDbUrlProvider {

    private final Path tempFolder;

    public SqliteTestFileDbUrlProvider() throws IOException {
        tempFolder = Files.createTempDirectory("sqlittest");
        tempFolder.toFile().deleteOnExit();
    }

    @Override
    protected void initDbForTest() throws IOException {
        Files.deleteIfExists(tempFolder.resolve("source.sqlite"));
        Files.deleteIfExists(tempFolder.resolve("target.sqlite"));
    }

    @Override
    protected String getSourceDbUrl() {
        return getDbUrl("source.sqlite");
    }

    @Override
    protected String getTargetDbUrl() {
        return getDbUrl("target.sqlite");
    }

    protected String getDbUrl(String dbFileName) {
        return "jdbc:sqlite:" + tempFolder.resolve(dbFileName).toAbsolutePath().toString();
    }
}

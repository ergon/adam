package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.Adam;
import ch.ergon.adam.integrationtest.sqlite.SqliteTestInMemoryDbUrlProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ch.ergon.adam.core.Adam.*;
import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jooq.SQLDialect.SQLITE;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbMigrationWithSqliteInMemoryTest extends AbstractDbTestBase {

    private static final String DB_VERSION_LAST = "4";
    private static final String DB_VERSION_FIRST = "1";

    public DbMigrationWithSqliteInMemoryTest() {
        super(new SqliteTestInMemoryDbUrlProvider(), SQLITE);
    }

    private Adam getDbMigration(String targetVersion) throws IOException {
        Path exportPath = Paths.get("../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE);
        Path targetVersionFile = exportPath.resolve(TARGET_VERSION_FILE_NAME);
        Files.write(targetVersionFile, targetVersion.getBytes(UTF_8));
        return Adam.usingExportDirectory(getTargetDbUrl(), "yml", exportPath.resolve("not_existing"), exportPath);
    }

    private void doMigrate(String targetVersion) throws IOException {
        getDbMigration(targetVersion).execute();
    }

    private String getCurrentSchemaVersion() throws SQLException {
        ResultSet result = getTargetDbConnection().createStatement().executeQuery(format("SELECT target_version FROM %s ORDER BY execution_started_at DESC", SCHEMA_VERSION_TABLE_NAME));
        assertTrue(result.next());
        return result.getString(1);
    }

    @Test
    public void testDbMigration() throws Exception {
        try (Connection connection = getTargetDbConnection()) {
            doMigrate(DB_VERSION_FIRST);
            assertThat(getCurrentSchemaVersion(), is(DB_VERSION_FIRST));
        }
    }
}

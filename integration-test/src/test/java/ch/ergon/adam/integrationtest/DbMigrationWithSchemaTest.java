package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.Adam;
import ch.ergon.adam.integrationtest.postgresql.AbstractPostgresqlTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_PACKAGE;
import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;
import static ch.ergon.adam.core.Adam.TARGET_VERSION_FILE_NAME;
import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbMigrationWithSchemaTest extends AbstractPostgresqlTestBase {

    private static final String DB_VERSION_5 = "5";
    private static final String DB_VERSION_4 = "4";

    private Adam getDbMigration(String targetVersion) throws IOException {
        Path exportPath = Paths.get("../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE);
        Path targetVersionFile = exportPath.resolve(TARGET_VERSION_FILE_NAME);
        Files.write(targetVersionFile, targetVersion.getBytes(UTF_8));
        return Adam.usingExportDirectory(getTargetDbUrl(), "yml", exportPath.resolve("schema"), exportPath);
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
    public void testPreMigrationCreatingTable() throws Exception {
        doMigrate(DB_VERSION_4);
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_4));
        getTargetDbConnection().createStatement().execute(format("DROP TABLE test_table"));
        doMigrate(DB_VERSION_5);
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_5));
    }

}

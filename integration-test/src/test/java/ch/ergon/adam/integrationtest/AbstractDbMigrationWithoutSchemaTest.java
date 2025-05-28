package ch.ergon.adam.integrationtest;

import ch.ergon.adam.core.Adam;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ch.ergon.adam.core.Adam.*;
import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractDbMigrationWithoutSchemaTest extends AbstractDbTestBase {

    public AbstractDbMigrationWithoutSchemaTest(TestDbUrlProvider testDbUrlProvider, SQLDialect dialect) {
        super(testDbUrlProvider, dialect);
    }

    private static final String DB_VERSION_UNKNOWN = "6";
    private static final String DB_VERSION_5 = "5";
    private static final String DB_VERSION_4 = "4";
    private static final String DB_VERSION_2 = "2";
    private static final String DB_VERSION_1 = "1";

    private Adam getDbMigration(String targetVersion) throws IOException {
        Path exportPath = Paths.get("../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE + getExportFolder());
        Path targetVersionFile = exportPath.resolve(TARGET_VERSION_FILE_NAME);
        Files.write(targetVersionFile, targetVersion.getBytes(UTF_8));
        return Adam.usingExportDirectory(getTargetDbUrl(), "yml", exportPath.resolve("not_existing"), exportPath);
    }

    protected String getExportFolder() {
        return "";
    }

    private void doMigrate(String targetVersion) throws IOException {
        getDbMigration(targetVersion).execute();
    }

    private String getCurrentSchemaVersion() throws SQLException {
        ResultSet result = executeQueryOnTargetDb(format("SELECT \"target_version\" FROM \"%s\" ORDER BY \"execution_started_at\" DESC", SCHEMA_VERSION_TABLE_NAME));
        assertTrue(result.next());
        return result.getString(1);
    }

    private int countMigrations() throws SQLException {
        ResultSet result = executeQueryOnTargetDb(format("SELECT count(*) FROM \"%s\"", SCHEMA_VERSION_TABLE_NAME));
        assertTrue(result.next());
        return result.getInt(1);
    }

    private void setCurrentSchemaVersion(String version) throws SQLException {
        executeOnTargetDb(format("UPDATE \"%s\" SET \"target_version\" = '%s'", SCHEMA_VERSION_TABLE_NAME, version));
    }

    @Test
    public void testDbMigration() throws Exception {
        doMigrate(DB_VERSION_2);
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_2));
        doMigrate(DB_VERSION_4);
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_4));
    }

    @Test
    public void testDbMigrationToUnknownVersion() throws Exception {
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            doMigrate(DB_VERSION_UNKNOWN);
        });
        assertThat(exception.getMessage(), is("Target version [" + DB_VERSION_UNKNOWN + "] is unknown."));
    }

    @Test
    public void testDbMigrationFromUnknownVersion() throws Exception {
        doMigrate(DB_VERSION_4);
        setCurrentSchemaVersion(DB_VERSION_UNKNOWN);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            doMigrate(DB_VERSION_4);
        });
        assertThat(exception.getMessage(), is("Current db version [" + DB_VERSION_UNKNOWN + "] is unknown."));
    }

    @Test
    public void testDbMigrationFromUnknownVersionAllowed() throws Exception {
        doMigrate(DB_VERSION_4);
        setCurrentSchemaVersion(DB_VERSION_UNKNOWN);
        Adam adam = getDbMigration(DB_VERSION_4);
        adam.setAllowUnknownDBVersion(true);
        adam.execute();
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_4));
    }

    @Test
    public void testDbMigrationBackwards() throws Exception {
        doMigrate(DB_VERSION_4);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            doMigrate(DB_VERSION_1);
        });
        assertThat(exception.getMessage(), is("DB version [" + DB_VERSION_4 + "] is not an ancestor of target version [" + DB_VERSION_1 + "]"));
    }

    @Test
    public void testDbMigrationBackwardsAllowed() throws Exception {
        doMigrate(DB_VERSION_4);
        Adam adam = getDbMigration(DB_VERSION_1);
        adam.setAllowNonForwardMigration(true);
        adam.execute();
        assertThat(getCurrentSchemaVersion(), is(DB_VERSION_1));
    }

    @Test
    public void testDbMigrationSameVersion() throws Exception {
        doMigrate(DB_VERSION_2);
        assertThat(countMigrations(), is(1));
        doMigrate(DB_VERSION_4);
        assertThat(countMigrations(), is(2));
        doMigrate(DB_VERSION_4);
        assertThat(countMigrations(), is(2));
    }

    @Test
    public void testDbMigrationSameVersionEnforced() throws Exception {
        doMigrate(DB_VERSION_2);
        assertThat(countMigrations(), is(1));
        doMigrate(DB_VERSION_4);
        assertThat(countMigrations(), is(2));
        Adam adam = getDbMigration(DB_VERSION_4);
        adam.setMigrateSameVersion(true);
        adam.execute();
        assertThat(countMigrations(), is(3));
    }


}

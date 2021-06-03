package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.Adam;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_PACKAGE;
import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;
import static ch.ergon.adam.core.Adam.TARGET_VERSION_FILE_NAME;
import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RollbackOnInsertErrorTest extends AbstractDbTestBase {

    private static final String DB_VERSION_2 = "2";
    private static final String DB_VERSION_4 = "4";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "id int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (1)";

    public RollbackOnInsertErrorTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }

    @Test
    public void testRollbackAfterAddFieldNotNullWithoutDefaultOnlySchemaMigration() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        //Initial migration
        migrateTargetWithSchema(schema);
        cleanupCloseables();

        // Apply change
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(INTEGER);
        fields.add(newField);
        table.setFields(fields);
        boolean hadException = false;
        try {
            migrateTargetWithSchema(schema);
        } catch (Exception e) {
            //Migration should fail... and do a rollback
            hadException = true;
        }
        assertTrue(hadException);

        // Data still present?
        cleanupCloseables();
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(id) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));

    }

    @Test
    public void testRollbackAfterAddFieldNotNullWithoutDefaultUsingMigrateDb() throws Exception {
        Path exportPath = Paths.get("../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE);
        Path targetVersionFile = exportPath.resolve(TARGET_VERSION_FILE_NAME);
        Files.write(targetVersionFile, DB_VERSION_2.getBytes(UTF_8));

        // Prepare db
        Adam adam = Adam.usingExportDirectory(getTargetDbUrl(), "yml", exportPath.resolve("schema"), exportPath);
        adam.execute();
        getTargetDbConnection().createStatement().execute("insert into  test_table (col1, col4) values (1, 'test')");
        cleanupCloseables();
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from " + SCHEMA_VERSION_TABLE_NAME);
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));

        // Failing migration
        Files.write(targetVersionFile, DB_VERSION_4.getBytes(UTF_8));
        adam = Adam.usingExportDirectory(getTargetDbUrl(), "yml", exportPath.resolve("schema_add_not_null_field"), exportPath);
        try {
            adam.execute();
            fail("Migration should throw an exception.");
        } catch (RuntimeException e) {
            //Migration should fail... and do a rollback
            assertThat(e.getMessage(), is("Migration failed."));
        }
        cleanupCloseables();
        result = getTargetDbConnection().createStatement().executeQuery("select count(*) from " + SCHEMA_VERSION_TABLE_NAME);
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));

    }

}

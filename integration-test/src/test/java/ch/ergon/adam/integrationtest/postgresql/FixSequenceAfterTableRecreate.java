package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.DataType;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.yml.YmlFactory;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;
import static ch.ergon.adam.core.Adam.DEFAULT_SCHEMA_PACKAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixSequenceAfterTableRecreate extends AbstractPostgresqlTestBase {

    private static final String INSERT_DATA_SQL_1 =
        "insert into test_table (col1, col4) values (1, 'test')";
    private static final String INSERT_DATA_SQL_2 =
        "insert into test_table (col1, col4) values (2, 'test')";

    @Test
    public void testSequenceAfterMigration() throws Exception {

        SchemaSource schemaSource = new YmlFactory().createSource("yml://../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_SCHEMA_PACKAGE);
        Schema sourceSchema = schemaSource.getSchema();
        migrateTargetWithSchema(sourceSchema);

        // Try to insert record
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL_1);

        //Force migration
        sourceSchema.getTable("test_table").getField("col3").setDataType(DataType.CLOB);
        migrateTargetWithSchema(sourceSchema);

        // Try to insert another record
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL_2);


        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }

    @Test
    public void testSequenceAfterMigrationWithEmptyTable() throws Exception {

        SchemaSource schemaSource = new YmlFactory().createSource("yml://../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_SCHEMA_PACKAGE);
        Schema sourceSchema = schemaSource.getSchema();
        migrateTargetWithSchema(sourceSchema);

        //Force migration
        sourceSchema.getTable("test_table").getField("col3").setDataType(DataType.CLOB);
        migrateTargetWithSchema(sourceSchema);

        // Try to insert record
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL_2);


        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }
}

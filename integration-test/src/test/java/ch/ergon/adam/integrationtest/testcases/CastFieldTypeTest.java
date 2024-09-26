package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static ch.ergon.adam.core.db.schema.DataType.VARCHAR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CastFieldTypeTest extends AbstractDbTestBase {

    protected static final String INSERT_DATA_SQL =
        "insert into \"test_table\" values ('2', 2)";

    public CastFieldTypeTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testCastVarcharToInt() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableStatement());
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field field = table.getField("col1");
        field.setDataType(INTEGER);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(\"col1\") from \"test_table\"");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }

    @Test
    public void testCastIntToVarchar() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableStatement());
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field field = table.getField("col2");
        field.setDataType(VARCHAR);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(\"col2\") from \"test_table\" where \"col2\"= '2'");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }

    protected String getCreateTableStatement() {
        return
            "create table \"test_table\" (" +
                "\"col1\" varchar, " +
                "\"col2\" int " +
                ")";
    }
}

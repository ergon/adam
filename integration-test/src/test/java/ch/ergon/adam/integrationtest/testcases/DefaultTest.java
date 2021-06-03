package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.schema.Schema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class DefaultTest extends AbstractDbTestBase {

    private static final String CREATE_TABLE_INT_DEFAULT_SQL =
        "create table test_table (" +
            "id bigint default 1 " +
            ")";

    private static final String CREATE_TABLE_STRING_DEFAULT_SQL =
        "create table test_table (" +
            "id VARCHAR default 'defaultValue' " +
            ")";

    private static final String CREATE_TABLE_FUNCTION_DEFAULT_SQL =
        "create table test_table (" +
            "id bigint default char_length('defaultValue') " +
            ")";

    public DefaultTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testIntDefault() throws Exception {
        doTestDefault(CREATE_TABLE_INT_DEFAULT_SQL, "1");
    }

    @Test
    public void testStringDefault() throws Exception {
        doTestDefault(CREATE_TABLE_STRING_DEFAULT_SQL, "'defaultValue'");
    }

    @Test
    public void testFunctionDefault() throws Exception {
        doTestDefault(CREATE_TABLE_FUNCTION_DEFAULT_SQL, "char_length('defaultValue')");
    }

    private void doTestDefault(String sql, String expectedDefault) throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(sql);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDefaultValue(), is(expectedDefault));
    }

    @Test
    public void testDropDefault() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_INT_DEFAULT_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        schema.getTable("test_table").getField("id").setDefaultValue(null);

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertNull(schema.getTable("test_table").getField("id").getDefaultValue());
    }

    @Test
    public void testChangeDefault() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_INT_DEFAULT_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        schema.getTable("test_table").getField("id").setDefaultValue("123");

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDefaultValue(), is("123"));
    }
}

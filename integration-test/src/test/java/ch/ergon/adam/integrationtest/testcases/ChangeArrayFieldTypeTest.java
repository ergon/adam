package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.db.schema.DataType.DECIMAL_INTEGER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ChangeArrayFieldTypeTest extends AbstractDbTestBase {

    private static final String CREATE_TABLE_WITH_ARRAY_SQL =
        "create table test_table (" +
            "test_field varchar[] not null " +
            ")";

    private static final String INSERT_SQL =
        "insert into test_table values ('{\"123\"}')";

    public ChangeArrayFieldTypeTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void changeArrayToArray() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_WITH_ARRAY_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("test_field").setDataType(DECIMAL_INTEGER);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("test_field").getDataType(), is(DECIMAL_INTEGER));
    }
}

package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.schema.Schema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class IndexTests extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
            "create table test_table (" +
            "id integer null unique, " +
            "col1 varchar" +
            ")";

    private static final String CREATE_PARTIAL_INDEX_SQL =
        "create unique index partial_idx on test_table(id) where col1 = 'test'";

    public IndexTests(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testCreateIndex() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getIndexes().size(), is(1));
    }

    @Test
    public void testRecreateIndexAfterTableChange() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_PARTIAL_INDEX_SQL);

        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("id").setNullable(false);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertFalse(schema.getTable("test_table").getField("id").isNullable());
        assertThat(schema.getTable("test_table").getIndexes().size(), is(2));
        assertThat(schema.getTable("test_table").getIndex("partial_idx").getWhere(), is("col1 = 'test'"));
    }
}

package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OracleConstraintTests extends AbstractOracleTestBase {

    private static final String CREATE_TABLE_SQL =
            "create table test_table (" +
            "id integer null CHECK (id > 0) " +
            ")";

    @Test
    public void testCreateConstraint() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();

        // Verify
        Schema schema = dummySink.getTargetSchema();
        assertThat(schema.getTable("TEST_TABLE").getConstraints().size(), is(1));
    }

    @Test
    public void testRecreateConstraintAfterTableChange() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("TEST_TABLE").getField("ID").setNullable(false);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertFalse(schema.getTable("TEST_TABLE").getField("ID").isNullable());
        assertThat(schema.getTable("TEST_TABLE").getConstraints().size(), is(1));
    }
}

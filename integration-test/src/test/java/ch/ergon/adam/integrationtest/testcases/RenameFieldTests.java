package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class RenameFieldTests extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "col1 int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (2)";

    public RenameFieldTests(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testSimpleRenameField() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field newColField = new Field("new_col");
        newColField.setSqlForNew("col1");
        newColField.setDataType(INTEGER);
        newColField.setNullable(true);
        table.setFields(Lists.newArrayList(newColField));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);
        assertThat(table.getFields().size(), is(1));
        newColField = table.getField("new_col");
        assertNotNull(newColField);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(new_col) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }

}

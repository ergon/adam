package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StableSequenceNameTest extends AbstractPostgresqlTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "col1 bigserial " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (nextval('test_table_col1_seq'))";

    @Test
    public void testStableSequenceAfterFullMigration() throws Exception {

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
        table.setFields(Lists.newArrayList(newColField, table.getField("col1")));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(col1) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(4));
    }

}

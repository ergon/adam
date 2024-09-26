package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.testcases.CastFieldTypeTest;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleCastFieldTypeTests extends CastFieldTypeTest {
    public OracleCastFieldTypeTests() {
        super(new OracleTestDbUrlProvider());
    }

    @Test
    public void testCastVarcharToSerial() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableStatement());
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field field = table.getField("col1");
        field.setDataType(INTEGER);
        field.setSequence(true);
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

    @Override
    protected String getCreateTableStatement() {
        return "create table \"test_table\" (" +
            "\"col1\" varchar2(100), " +
            "\"col2\" int " +
            ")";
    }
}

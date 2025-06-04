package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.DataType;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.jooq.SQLDialect.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumArrayFieldTest extends AbstractDbTestBase {

    public EnumArrayFieldTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }

    private static final String CREATE_ENUM_SQL =
        "create type test_enum as enum ('val1')";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "array_col test_enum[]" +
            ")";


    private static final String INSERT_SQL =
        "insert into test_Table values ('{\"val1\"}')";

    @Test
    public void testCreateTableToYml() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Field arrayColField = dummySink.getTargetSchema().getTable("test_table").getField("array_col");
        assertTrue(arrayColField.isArray());
        assertThat(arrayColField.getDataType(), is(DataType.ENUM));
        assertThat(arrayColField.getDbEnum().getName(), is("test_enum"));
    }

    @Test
    public void testChangeArrayEnum() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getEnum("test_enum").setValues(new String[]{"val1", "val2"});
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getEnum("test_enum").getValues(), arrayContaining("val1", "val2"));
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(1) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), CoreMatchers.is(1));
    }

}

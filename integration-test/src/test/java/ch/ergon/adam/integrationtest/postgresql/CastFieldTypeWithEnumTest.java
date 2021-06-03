package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.core.db.schema.DbEnum;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CastFieldTypeWithEnumTest extends AbstractPostgresqlTestBase {

    private static final String CREATE_ENUM_SQL =
        "create type test_enum as enum ('val1')";

    private static final String CREATE_ENUM_NEW_SQL =
        "create type test_enum_new as enum ('val1')";


    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "col3 varchar, " +
            "col4 test_enum " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values ('val1', 'val1')";

    @Test
    public void testCastVarcharToEnum() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_NEW_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field field = table.getField("col3");
        field.setDataType(ENUM);
        field.setDbEnum(new DbEnum("test_enum_new"));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(col3) from test_table where col3= 'val1'");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }

    @Test
    public void testCastEnumToEnum() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_NEW_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field field = table.getField("col4");
        field.setDataType(ENUM);
        field.setDbEnum(new DbEnum("test_enum_new"));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(col4) from test_table where col4= 'val1'");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }


}

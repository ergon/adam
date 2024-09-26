package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnumTests extends AbstractPostgresqlTestBase {

    private static final String CREATE_ENUM_SQL =
            "create type test_enum as enum ('val1')";

    private static final String CREATE_TABLE_SQL =
            "create table test_table (" +
                "id test_enum, " +
                "not_null_enum test_enum not null," +
                "enum_with_default test_enum default 'val1'" +
            ")";

    @Test
    public void testCreateEnum() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDbEnum().getName(), is("test_enum"));
        assertThat(schema.getEnums().size(), is(1));
        assertFalse(schema.getTable("test_table").getField("not_null_enum").isNullable());
        assertNotNull(schema.getTable("test_table").getField("enum_with_default").getDefaultValue());
    }

    @Test
    public void testRecreateTableAfterEnumChange() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getEnum("test_enum").setValues(new String[] {"val1", "val2"});
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDbEnum().getName(), is("test_enum"));
        assertThat(schema.getEnums().size(), is(1));
    }
}

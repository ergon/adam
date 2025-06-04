package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.core.db.schema.DbEnum;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MariaDbEnumTests extends AbstractMariaDbTestBase {

    private static final String CREATE_TABLE_SQL =
            "create table test_table (" +
                "id enum('val1', 'val2'), " +
                "not_null_enum enum('val1', 'val2') not null," +
                "enum_with_default enum('val1', 'val2') default 'val1'" +
            ")";

    @Test
    public void testCreateEnum() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDbEnum().getName(), is("test_table_id"));
        assertThat(schema.getEnums().size(), is(3));
        assertFalse(schema.getTable("test_table").getField("not_null_enum").isNullable());
        assertNotNull(schema.getTable("test_table").getField("enum_with_default").getDefaultValue());
    }

    @Test
    public void testRecreateTableAfterEnumChange() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getEnum("test_table_id").setValues(new String[] {"val1", "val2", "val3"});
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        DbEnum dbEnum = schema.getTable("test_table").getField("id").getDbEnum();
        assertThat(dbEnum.getName(), is("test_table_id"));
        assertThat(dbEnum.getValues().length, is(3));
        assertThat(schema.getTable("test_table").getField("not_null_enum").getDbEnum().getValues().length, is(2));
        assertThat(schema.getEnums().size(), is(3));
    }
}

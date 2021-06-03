package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static ch.ergon.adam.core.db.schema.DataType.NUMERIC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SqliteCreateTableTest extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "col1 numeric not null UNIQUE, " +
            "col2 numeric null default 10, " + // Precision and scale are not applied by sqlite
            "col3 text null, " +
            "col4 varchar not null " + // Length is not applied by sqlite
            ")";

    public SqliteCreateTableTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    private void verifySchema(Schema schema) {
        assertNotNull(schema);
        assertThat(schema.getTables().size(), is(1));
        assertTrue(schema.getViews().isEmpty());
        assertTrue(schema.getEnums().isEmpty());

        Table table = schema.getTable("test_table");
        assertNotNull(table);
        assertThat(table.getFields().size(), is(5));

        Field field = table.getField("id");
        assertNotNull(field);
        assertThat(field.getIndex(), is(0));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertTrue(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(INTEGER));

        field = table.getField("col1");
        assertNotNull(field);
        assertThat(field.getIndex(), is(1));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(NUMERIC));

        field = table.getField("col2");
        assertNotNull(field);
        assertThat(field.getIndex(), is(2));
        assertTrue(field.isNullable());
        assertFalse(field.isArray());
        assertFalse(field.isSequence());
        assertThat(field.getDefaultValue(), is("10"));
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(NUMERIC));

        field = table.getField("col3");
        assertNotNull(field);
        assertThat(field.getIndex(), is(3));
        assertTrue(field.isNullable());
        assertFalse(field.isArray());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DataType.CLOB));

        field = table.getField("col4");
        assertNotNull(field);
        assertThat(field.getIndex(), is(4));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DataType.VARCHAR));

        Index index = table.getIndex("pk");
        assertNotNull(index);
        assertTrue(index.isPrimary());
        assertTrue(index.isUnique());
        assertThat(index.getFields().size(), is(1));
        assertThat(index.getFields().get(0).getName(), is("id"));

        index = table.getIndex("test_table_col1_idx");
        assertNotNull(index);
        assertFalse(index.isPrimary());
        assertTrue(index.isUnique());
        assertThat(index.getFields().size(), is(1));
        assertThat(index.getFields().get(0).getName(), is("col1"));
    }

    @Test
    public void testCreateTable() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        verifySchema(schema);
    }
}

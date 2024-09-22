package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.db.schema.DataType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PostgreSqlCreateTableTest extends AbstractDbTestBase {

    public PostgreSqlCreateTableTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }

    private static final String YML = "---\n" +
        "name: \"test_table\"\n" +
        "fields:\n" +
        "- name: \"id\"\n" +
        "  dataType: \"BIGINT\"\n" +
        "  sequence: true\n" +
        "- name: \"col1\"\n" +
        "  dataType: \"DECIMAL_INTEGER\"\n" +
        "- name: \"col2\"\n" +
        "  dataType: \"NUMERIC\"\n" +
        "  defaultValue: \"10\"\n" +
        "  nullable: true\n" +
        "  precision: 10\n" +
        "  scale: 2\n" +
        "- name: \"col3\"\n" +
        "  dataType: \"CLOB\"\n" +
        "  nullable: true\n" +
        "- name: \"col4\"\n" +
        "  dataType: \"VARCHAR\"\n" +
        "  length: 10\n" +
        "- name: \"col5\"\n" +
        "  array: true\n" +
        "  dataType: \"VARCHAR\"\n" +
        "  length: 10\n" +
        "indexes:\n" +
        "- name: \"test_table_col1_key\"\n" +
        "  fields:\n" +
        "  - \"col1\"\n" +
        "  unique: true\n" +
        "- name: \"test_table_pkey\"\n" +
        "  fields:\n" +
        "  - \"id\"\n" +
        "  primary: true\n" +
        "  unique: true\n" +
        "--- []\n" +
        "--- []\n";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "id bigserial PRIMARY KEY, " +
            "col1 numeric not null UNIQUE, " +
            "col2 numeric(10,2) null default 10, " +
            "col3 text null, " +
            "col4 varchar(10) not null, " +
            "col5 varchar(10)[] not null" +
            ")";

    private void verifySchema(Schema schema) {
        assertNotNull(schema);
        assertThat(schema.getTables().size(), is(1));
        assertTrue(schema.getViews().isEmpty());
        assertTrue(schema.getEnums().isEmpty());

        Table table = schema.getTable("test_table");
        assertNotNull(table);
        assertThat(table.getFields().size(), is(6));

        Field field = table.getField("id");
        assertNotNull(field);
        assertThat(field.getIndex(), is(0));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertNull(field.getLength());
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertTrue(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(BIGINT));

        field = table.getField("col1");
        assertNotNull(field);
        assertThat(field.getIndex(), is(1));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertNull(field.getLength());
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DECIMAL_INTEGER));

        field = table.getField("col2");
        assertNotNull(field);
        assertThat(field.getIndex(), is(2));
        assertTrue(field.isNullable());
        assertFalse(field.isArray());
        assertNull(field.getLength());
        assertThat(field.getPrecision(), is(10));
        assertThat(field.getScale(), is(2));
        assertFalse(field.isSequence());
        assertThat(field.getDefaultValue(), is("10"));
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(NUMERIC));

        field = table.getField("col3");
        assertNotNull(field);
        assertThat(field.getIndex(), is(3));
        assertTrue(field.isNullable());
        assertFalse(field.isArray());
        assertNull(field.getLength());
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DataType.CLOB));

        field = table.getField("col4");
        assertNotNull(field);
        assertThat(field.getIndex(), is(4));
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertThat(field.getLength(), is(10));
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DataType.VARCHAR));

        field = table.getField("col5");
        assertNotNull(field);
        assertThat(field.getIndex(), is(5));
        assertFalse(field.isNullable());
        assertTrue(field.isArray());
        assertThat(field.getLength(), is(10));
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertFalse(field.isSequence());
        assertNull(field.getDefaultValue());
        assertNull(field.getDbEnum());
        assertThat(field.getDataType(), is(DataType.VARCHAR));

        Index index = table.getIndex("test_table_pkey");
        assertNotNull(index);
        assertTrue(index.isPrimary());
        assertTrue(index.isUnique());
        assertThat(index.getFields().size(), is(1));
        assertThat(index.getFields().get(0).getName(), is("id"));

        index = table.getIndex("test_table_col1_key");
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

    @Test
    public void testCreateTableToYml() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        String yml = targetToYml();
        assertEquals(YML, yml);
    }

}

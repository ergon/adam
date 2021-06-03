package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static ch.ergon.adam.core.db.schema.DataType.CLOB;
import static ch.ergon.adam.core.db.schema.DataType.INTEGER;
import static ch.ergon.adam.core.db.schema.DataType.VARCHAR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AddFieldTests extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "id int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (1)";

    public AddFieldTests(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testAddFieldAtEndOfTable() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(VARCHAR);
        newField.setDefaultValue("\'testDefault\'");
        fields.add(newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertThat(table.getFields().size(), is(2));
        Field field = table.getField("new_field");
        assertNotNull(field);
        assertThat(field.getDataType(), is(VARCHAR));
        assertFalse(field.isSequence());
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertThat(field.getIndex(), is(1));
        assertThat(field.getDefaultValue(), is("'testDefault'"));

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));

    }

    @Test
    public void testAddFieldAtBeginOfTable() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(CLOB);
        newField.setDefaultValue("\'abcd\'"); // Not-null field needs to have a default
        fields.add(0, newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertThat(table.getFields().size(), is(2));
        Field field = table.getField("new_field");
        assertNotNull(field);
        assertThat(field.getDataType(), is(CLOB));
        assertFalse(field.isSequence());
        assertFalse(field.isNullable());
        assertFalse(field.isArray());
        assertThat(field.getIndex(), is(0));
        assertNull(field.getLength());
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertThat(field.getDefaultValue(), is("'abcd'"));

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }

    @Test
    public void testAddFieldAtEndOfTableWithSimpleMigrationNotNullWithDefault() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(INTEGER);
        newField.setDefaultValue("1");
        newField.setSqlForNew("id");
        fields.add(newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(new_field) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));

    }

    @Test
    public void testAddFieldAtEndOfTableWithComplexMigrationNotNullWithoutDefault() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(INTEGER);
        newField.setSqlForNew("id + 1");
        fields.add(newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(new_field) from test_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));

    }
}

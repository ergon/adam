package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static ch.ergon.adam.core.db.schema.DataType.CLOB;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public abstract class ChangeFieldTypeTest extends AbstractDbTestBase {

    protected String getCreateTableSerialSql() {
        return "create table test_table (" +
            "id bigserial " +
            ")";
    }

    protected String getCreateTableNotNullSql() {
        return "create table test_table (" +
            "test_field varchar not null " +
            ")";
    }

    protected String getCreateTableNullSql() {
        return "create table test_table (" +
            "test_field numeric(10,2) null " +
            ")";
    }

    protected String getCreateTableTwoFieldsSql() {
        return "create table test_table (" +
            "col1 text null, " +
            "col2 text null " +
            ")";
    }

    public ChangeFieldTypeTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void changeFromSerialToClob() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableSerialSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        Field idField = table.getField("id");
        idField.setSequence(false);
        idField.setDataType(CLOB);
        idField.setNullable(true);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertThat(table.getFields().size(), is(1));
        Field field = table.getField("id");
        assertNotNull(field);
        assertThat(field.getDataType(), is(CLOB));
        assertFalse(field.isSequence());
        assertTrue(field.isNullable());
        assertFalse(field.isArray());
        assertThat(field.getIndex(), is(0));
        assertNull(field.getLength());
        assertNull(field.getPrecision());
        assertNull(field.getScale());
        assertNull(field.getDefaultValue());
    }

    @Test
    public void changeNotNullToNull() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableNotNullSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("test_field").setNullable(true);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertTrue(schema.getTable("test_table").getField("test_field").isNullable());
    }

    @Test
    public void changeNullToNotNull() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableNullSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("test_field").setNullable(false);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertFalse(schema.getTable("test_table").getField("test_field").isNullable());
    }

    @Test
    public void changePrecision() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableNullSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("test_field").setPrecision(3);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("test_field").getPrecision(), is(3));
    }

    @Test
    public void changeScale() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableNullSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        schema.getTable("test_table").getField("test_field").setScale(3);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("test_field").getScale(), is(3));
    }

    @Test
    public void changeFieldOrder() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(getCreateTableTwoFieldsSql());
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        ArrayList<Field> fields = new ArrayList<>(table.getFields());
        Collections.reverse(fields);
        table.setFields(fields);
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("col2").getIndex(), is(0));
    }
}

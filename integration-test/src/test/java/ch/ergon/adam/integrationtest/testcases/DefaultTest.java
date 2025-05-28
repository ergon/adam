package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class DefaultTest extends AbstractDbTestBase {

    protected String getCreateTableIntDefaultSql() {
        return "create table test_table (" +
            "id bigint default 1 " +
            ")";
    }

    protected String getCreateTableStringDefaultSql() {
        return "create table test_table (" +
            "id VARCHAR default 'defaultValue' " +
            ")";
    }

    protected String getCreateTableFunctionDefaultSql() {
        return "create table test_table (" +
            "id bigint default char_length('defaultValue') " +
            ")";
    }

    public DefaultTest(TestDbUrlProvider testDbUrlProvider, SQLDialect dialect) {
        super(testDbUrlProvider, dialect);
    }

    @Test
    public void testIntDefault() throws Exception {
        doTestDefault(getCreateTableIntDefaultSql(), "1");
    }

    @Test
    public void testStringDefault() throws Exception {
        doTestDefault(getCreateTableStringDefaultSql(), "'defaultValue'");
    }

    @Test
    public void testFunctionDefault() throws Exception {
        doTestDefault(getCreateTableFunctionDefaultSql(), "char_length('defaultValue')");
    }

    protected void doTestDefault(String sql, String expectedDefault) throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(sql);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDefaultValue(), is(expectedDefault));
    }

    @Test
    public void testDropDefault() throws Exception {
        getSourceDbConnection().createStatement().execute(getCreateTableIntDefaultSql());
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        schema.getTable("test_table").getField("id").setDefaultValue(null);

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertNull(schema.getTable("test_table").getField("id").getDefaultValue());
    }

    @Test
    public void testChangeDefault() throws Exception {
        getSourceDbConnection().createStatement().execute(getCreateTableIntDefaultSql());
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        schema.getTable("test_table").getField("id").setDefaultValue("123");

        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        assertThat(schema.getTable("test_table").getField("id").getDefaultValue(), is("123"));
    }
}

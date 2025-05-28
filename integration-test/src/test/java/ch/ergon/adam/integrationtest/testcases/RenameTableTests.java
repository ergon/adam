package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ch.ergon.adam.core.db.schema.DataType.CLOB;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class RenameTableTests extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
            "create table test_table (" +
                "id int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (1)";

    public RenameTableTests(TestDbUrlProvider testDbUrlProvider, SQLDialect dialect) {
        super(testDbUrlProvider, dialect);
    }

    @Test
    public void testSimpleRenameTable() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema oldSchema = dummySink.getTargetSchema();

        // Apply change
        Table oldTable = oldSchema.getTable("test_table");
        Table table = new Table("new_table_name");
        table.setPreviousName("test_table");
        Collection<Field> fields = oldTable.getFields();
        table.setFields(fields);
        Schema schema = new Schema();
        schema.setTables(newArrayList(table));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("new_table_name");
        assertNotNull(table);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from new_table_name");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }

    @Test
    public void testRenameWithRebuildTable() throws Exception {

        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema oldSchema = dummySink.getTargetSchema();

        // Apply change
        Table oldTable = oldSchema.getTable("test_table");
        Table table = new Table("new_table_name");
        table.setPreviousName("test_table");
        List<Field> fields = new ArrayList<>(oldTable.getFields());
        Field newField = new Field("new_field");
        newField.setDataType(CLOB);
        newField.setNullable(true);
        fields.add(0, newField);
        table.setFields(fields);
        Schema schema = new Schema();
        schema.setTables(newArrayList(table));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("new_table_name");
        assertNotNull(table);
        newField = table.getField("new_field");
        assertNotNull(newField);

        // Data still present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select count(*) from new_table_name");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(1));
    }

}

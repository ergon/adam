package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class RemoveFieldTests extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table \"test_table\" (" +
            "\"col1\" int, " +
            "\"col2\" int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into \"test_table\" values (2, 3)";

    public RemoveFieldTests(TestDbUrlProvider testDbUrlProvider, SQLDialect dialect) {
        super(testDbUrlProvider, dialect);
    }

    @Test
    public void testSimpleRemoveField() throws Exception {

        // Setup db
        executeOnTargetDb(CREATE_TABLE_SQL);
        executeOnTargetDb(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table table = schema.getTable("test_table");
        table.setFields(table.getFields().stream().filter(field -> !field.getName().equals("col2")).collect(toList()));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        table = schema.getTable("test_table");
        assertNotNull(table);
        assertThat(table.getFields().size(), is(1));

        // Data still present?
        ResultSet result = executeQueryOnTargetDb("select sum(\"col1\") from \"test_table\"");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }

    @Test
    public void testSimpleRemoveFieldWithTableRename() throws Exception {

        // Setup db
        executeOnTargetDb(CREATE_TABLE_SQL);
        executeOnTargetDb(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Apply change
        Table oldTable = schema.getTable("test_table");
        Table newTable = new Table("new_table");
        newTable.setPreviousName("test_table");
        newTable.setFields(oldTable.getFields().stream().filter(field -> !field.getName().equals("col2")).collect(toList()));
        schema.setTables(newArrayList(newTable));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // Verify
        newTable = schema.getTable("new_table");
        assertNotNull(newTable);
        assertThat(newTable.getFields().size(), is(1));

        // Data still present?
        ResultSet result = executeQueryOnTargetDb("select sum(\"col1\") from \"new_table\"");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }

}

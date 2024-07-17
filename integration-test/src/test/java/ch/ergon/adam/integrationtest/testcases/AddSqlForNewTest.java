package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.schema.DbEnum;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AddSqlForNewTest extends AbstractDbTestBase {

    public AddSqlForNewTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    private static final String CREATE_ENUM_SQL =
        "create type custom_enum as enum ('val1', 'val2')";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "col1 int" +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (1)";

    @Test
    public void testAddSqlForNewMigration() throws Exception {
        // Setup db
        getTargetDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getTargetDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getTargetDbConnection().createStatement().execute(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Add field
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("custom_type");
        newField.setDataType(ENUM);
        newField.setDbEnum(new DbEnum("custom_enum"));
        newField.setArray(true);
        newField.setDefaultValue("'{val2}'");
        newField.setSqlForNew("'{val1}'");
        fields.add(newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);

        // Verify
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select * from test_table");
        assertTrue(result.next());
        assertThat(result.getString(2), is("{val1}"));
    }
}

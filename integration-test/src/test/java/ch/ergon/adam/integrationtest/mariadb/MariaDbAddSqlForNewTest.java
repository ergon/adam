package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.core.db.schema.DbEnum;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MariaDbAddSqlForNewTest extends AbstractMariaDbTestBase {
    public MariaDbAddSqlForNewTest() {
        super();
    }

    private static final String CREATE_TABLE_SQL =
        "create table \"test_table\" (" +
            "col1 int" +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into \"test_table\" values (1)";

    @Test
    public void testAddSqlForNewMigration() throws Exception {
        // Setup db
        executeOnTargetDb(CREATE_TABLE_SQL);
        executeOnTargetDb(INSERT_DATA_SQL);
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Add field
        Table table = schema.getTable("test_table");
        List<Field> fields = new ArrayList<>(table.getFields());
        Field newField = new Field("custom_type");
        newField.setDataType(ENUM);
        newField.setDbEnum(new DbEnum("custom_enum"));
        newField.setDefaultValue("'val2'");
        newField.setSqlForNew("'val1'");
        fields.add(newField);
        table.setFields(fields);
        migrateTargetWithSchema(schema);

        // Verify
        ResultSet result = executeQueryOnTargetDb("select * from \"test_table\"");
        assertTrue(result.next());
        assertThat(result.getString(2), is("val1"));
    }
}

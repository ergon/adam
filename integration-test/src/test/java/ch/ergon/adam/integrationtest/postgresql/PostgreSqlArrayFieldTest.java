package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Test;

import static org.jooq.SQLDialect.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgreSqlArrayFieldTest extends AbstractDbTestBase {

    public PostgreSqlArrayFieldTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }

    private static final String YML = "---\n" +
        "name: \"test_table\"\n" +
        "fields:\n" +
        "- name: \"array_col\"\n" +
        "  array: true\n" +
        "  dataType: \"VARCHAR\"\n" +
        "  defaultValue: \"'{}'\"\n" +
        "  length: 10\n" +
        "--- []\n" +
        "--- []\n";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "array_col varchar(10)[] not null" + // default '{}'
            ")";

    @Test
    public void testCreateTableToYml() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        schema.getTable("test_table").getField("array_col").setDefaultValue("'{}'");
        migrateTargetWithSchema(schema);
        String yml = targetToYml();
        assertEquals(YML, yml);
    }

}

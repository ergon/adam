package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlIntervalFieldTest extends AbstractDbTestBase {
    public PostgreSqlIntervalFieldTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }

    private static final String YML = "---\n" +
        "name: \"test_table\"\n" +
        "fields:\n" +
        "- name: \"duration\"\n" +
        "  dataType: \"INTERVALYEARTOSECOND\"\n" +
        "--- []\n" +
        "--- []\n";

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "duration interval not null" +
            ")";

    @Test
    public void testCreateTableToYml() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        migrateTargetWithSchema(schema);
        String yml = targetToYml();
        assertThat(yml, is(YML));
    }

}

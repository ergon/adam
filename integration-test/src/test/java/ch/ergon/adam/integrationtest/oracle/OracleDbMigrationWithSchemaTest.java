package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;
import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;

public class OracleDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public OracleDbMigrationWithSchemaTest() {
        super (new OracleTestDbUrlProvider());
    }

    @Override
    protected String getExportFolder() {
        return "oracle/";
    }
}

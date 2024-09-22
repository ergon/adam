package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;
import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;

public class OracleDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public OracleDbMigrationWithoutSchemaTest() {
        super (new OracleTestDbUrlProvider());
    }
}

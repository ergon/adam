package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;
import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;

import static org.jooq.SQLDialect.ORACLE;

public class OracleDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public OracleDbMigrationWithoutSchemaTest() {
        super (new OracleTestDbUrlProvider(), ORACLE);
    }
}

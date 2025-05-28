package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;
import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;

import static org.jooq.SQLDialect.ORACLE;

public class OracleDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public OracleDbMigrationWithSchemaTest() {
        super (new OracleTestDbUrlProvider(), ORACLE);
    }

    @Override
    protected String getExportFolder() {
        return "oracle/";
    }
}

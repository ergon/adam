package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public MariaDbMigrationWithSchemaTest() {
        super (new MariaDbTestDbUrlProvider(), MARIADB);
    }

    @Override
    protected String getExportFolder() {
        return "mariadb/";
    }
}

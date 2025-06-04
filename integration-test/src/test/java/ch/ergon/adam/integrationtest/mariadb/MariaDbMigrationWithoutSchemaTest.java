package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public MariaDbMigrationWithoutSchemaTest() {
        super (new MariaDbTestDbUrlProvider(), MARIADB);
    }

    protected String getExportFolder() {
        return "mariadb/";
    }
}

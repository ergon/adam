package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;

public class MariaDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public MariaDbMigrationWithSchemaTest() {
        super (new MariaDbTestDbUrlProvider());
    }

    @Override
    protected String getExportFolder() {
        return "oracle/";
    }
}

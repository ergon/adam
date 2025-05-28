package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;

public class MariaDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public MariaDbMigrationWithoutSchemaTest() {
        super (new MariaDbTestDbUrlProvider());
    }
}

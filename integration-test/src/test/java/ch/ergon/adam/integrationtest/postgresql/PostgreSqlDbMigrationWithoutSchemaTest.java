package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;

public class PostgreSqlDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public PostgreSqlDbMigrationWithoutSchemaTest() {
        super (new PostgreSqlTestDbUrlProvider());
    }
}

package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;

public class PostgreSqlDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public PostgreSqlDbMigrationWithSchemaTest() {
        super (new PostgreSqlTestDbUrlProvider());
    }
}

package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithoutSchemaTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlDbMigrationWithoutSchemaTest extends AbstractDbMigrationWithoutSchemaTest {
    public PostgreSqlDbMigrationWithoutSchemaTest() {
        super (new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbMigrationWithSchemaTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlDbMigrationWithSchemaTest extends AbstractDbMigrationWithSchemaTest {
    public PostgreSqlDbMigrationWithSchemaTest() {
        super (new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

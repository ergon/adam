package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlRenameFieldTests extends RenameFieldTests {
    public PostgreSqlRenameFieldTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

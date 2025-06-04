package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RenameTableTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlRenameTableTests extends RenameTableTests {
    public PostgreSqlRenameTableTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

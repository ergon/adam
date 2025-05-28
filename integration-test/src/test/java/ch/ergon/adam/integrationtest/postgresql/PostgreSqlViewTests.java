package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ViewTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlViewTests extends ViewTests {
    public PostgreSqlViewTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

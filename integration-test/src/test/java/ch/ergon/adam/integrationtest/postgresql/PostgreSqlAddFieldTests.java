package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlAddFieldTests extends AddFieldTests {
    public PostgreSqlAddFieldTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

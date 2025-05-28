package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.IndexTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlIndexTests extends IndexTests {
    public PostgreSqlIndexTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

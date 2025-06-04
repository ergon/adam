package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlRemoveFieldTests extends RemoveFieldTests {
    public PostgreSqlRemoveFieldTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

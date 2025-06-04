package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.AddSqlForNewTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlAddSqlForNewTest extends AddSqlForNewTest {
    public PostgreSqlAddSqlForNewTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

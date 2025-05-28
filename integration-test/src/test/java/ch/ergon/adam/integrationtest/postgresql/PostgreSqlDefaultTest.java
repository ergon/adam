package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlDefaultTest extends DefaultTest {
    public PostgreSqlDefaultTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

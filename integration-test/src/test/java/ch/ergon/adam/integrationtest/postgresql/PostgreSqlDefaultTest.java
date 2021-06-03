package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;

public class PostgreSqlDefaultTest extends DefaultTest {
    public PostgreSqlDefaultTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}

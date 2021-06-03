package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ChangeArrayFieldTypeTest;

public class PostgreSqlChangeArrayFieldTypeTest extends ChangeArrayFieldTypeTest {
    public PostgreSqlChangeArrayFieldTypeTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}

package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ChangeFieldTypeTest;

public class PostgreSqlChangeFieldTypeTest extends ChangeFieldTypeTest {
    public PostgreSqlChangeFieldTypeTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}

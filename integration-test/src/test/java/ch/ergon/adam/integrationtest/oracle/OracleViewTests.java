package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;
import ch.ergon.adam.integrationtest.testcases.ViewTests;

public class OracleViewTests extends ViewTests {
    public OracleViewTests() {
        super(new OracleTestDbUrlProvider());
    }
}

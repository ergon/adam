package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

public class OracleRemoveFieldTests extends RemoveFieldTests {
    public OracleRemoveFieldTests() {
        super(new OracleTestDbUrlProvider());
    }
}

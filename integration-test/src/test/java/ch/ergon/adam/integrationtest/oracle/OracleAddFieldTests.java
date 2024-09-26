package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

public class OracleAddFieldTests extends AddFieldTests {
    public OracleAddFieldTests() {
        super(new OracleTestDbUrlProvider());
    }
}

package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

public class OracleRenameFieldTests extends RenameFieldTests {
    public OracleRenameFieldTests() {
        super(new OracleTestDbUrlProvider());
    }
}

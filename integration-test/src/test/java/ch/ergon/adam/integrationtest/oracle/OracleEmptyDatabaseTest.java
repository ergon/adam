package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

public class OracleEmptyDatabaseTest extends EmptyDatabaseTest {

    public OracleEmptyDatabaseTest() {
        super(new OracleTestDbUrlProvider());
    }
}

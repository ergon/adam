package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

public class AbstractOracleTestBase extends AbstractDbTestBase {


    public AbstractOracleTestBase() {
        super(new OracleTestDbUrlProvider());
    }
}

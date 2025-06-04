package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

import static org.jooq.SQLDialect.ORACLE;

public class AbstractOracleTestBase extends AbstractDbTestBase {
    public AbstractOracleTestBase() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}

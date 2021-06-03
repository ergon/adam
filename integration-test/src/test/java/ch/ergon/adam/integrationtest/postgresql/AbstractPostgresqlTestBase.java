package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

public class AbstractPostgresqlTestBase extends AbstractDbTestBase {


    public AbstractPostgresqlTestBase() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}

package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

import static org.jooq.SQLDialect.POSTGRES;

public class AbstractPostgresqlTestBase extends AbstractDbTestBase {
    public AbstractPostgresqlTestBase() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}

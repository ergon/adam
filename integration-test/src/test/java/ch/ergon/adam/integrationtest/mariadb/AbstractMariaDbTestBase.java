package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

import static org.jooq.SQLDialect.MARIADB;

public class AbstractMariaDbTestBase extends AbstractDbTestBase {
    public AbstractMariaDbTestBase() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbEmptyDatabaseTest extends EmptyDatabaseTest {
    public MariaDbEmptyDatabaseTest() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

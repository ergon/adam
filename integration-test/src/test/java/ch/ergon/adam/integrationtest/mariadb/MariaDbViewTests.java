package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.ViewTests;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbViewTests extends ViewTests {
    public MariaDbViewTests() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

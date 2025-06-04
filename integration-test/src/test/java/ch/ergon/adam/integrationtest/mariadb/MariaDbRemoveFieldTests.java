package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbRemoveFieldTests extends RemoveFieldTests {
    public MariaDbRemoveFieldTests() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

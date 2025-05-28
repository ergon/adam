package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbRenameFieldTests extends RenameFieldTests {
    public MariaDbRenameFieldTests() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

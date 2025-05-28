package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

public class MariaDbRenameFieldTests extends RenameFieldTests {
    public MariaDbRenameFieldTests() {
        super(new MariaDbTestDbUrlProvider());
    }
}

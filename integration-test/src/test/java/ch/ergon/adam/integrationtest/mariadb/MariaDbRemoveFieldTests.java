package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

public class MariaDbRemoveFieldTests extends RemoveFieldTests {
    public MariaDbRemoveFieldTests() {
        super(new MariaDbTestDbUrlProvider());
    }
}

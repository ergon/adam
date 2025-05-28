package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.ViewTests;

public class MariaDbViewTests extends ViewTests {
    public MariaDbViewTests() {
        super(new MariaDbTestDbUrlProvider());
    }
}

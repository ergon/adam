package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

public class MariaDbAddFieldTests extends AddFieldTests {
    public MariaDbAddFieldTests() {
        super(new MariaDbTestDbUrlProvider());
    }
}

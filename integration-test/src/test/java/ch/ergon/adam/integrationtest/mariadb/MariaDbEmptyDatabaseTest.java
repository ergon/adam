package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

public class MariaDbEmptyDatabaseTest extends EmptyDatabaseTest {

    public MariaDbEmptyDatabaseTest() {
        super(new MariaDbTestDbUrlProvider());
    }
}

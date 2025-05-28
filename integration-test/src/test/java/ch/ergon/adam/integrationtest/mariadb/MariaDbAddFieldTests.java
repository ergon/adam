package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbAddFieldTests extends AddFieldTests {
    public MariaDbAddFieldTests() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }
}

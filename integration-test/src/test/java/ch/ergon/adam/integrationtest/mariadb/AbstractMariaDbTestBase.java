package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;

public class AbstractMariaDbTestBase extends AbstractDbTestBase {
    public AbstractMariaDbTestBase() {
        super(new MariaDbTestDbUrlProvider());
    }
}

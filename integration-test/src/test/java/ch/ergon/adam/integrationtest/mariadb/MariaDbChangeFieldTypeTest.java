package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.ChangeFieldTypeTest;
import org.junit.jupiter.api.Disabled;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbChangeFieldTypeTest extends ChangeFieldTypeTest {
    public MariaDbChangeFieldTypeTest() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }

    @Override
    protected String getCreateTableNotNullSql() {
        return "CREATE TABLE test_table (" +
            "test_field VARCHAR(255) NOT NULL" +
            ")";
    }

    @Override
    protected String getCreateTableSerialSql() {
        return "CREATE TABLE test_table (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY" +
            ")";
    }

    @Override
    @Disabled
    public void changeFromSerialToClob() {
    }
}

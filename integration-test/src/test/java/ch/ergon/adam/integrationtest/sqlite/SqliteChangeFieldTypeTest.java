package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.ChangeFieldTypeTest;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

public class SqliteChangeFieldTypeTest extends ChangeFieldTypeTest {
    public SqliteChangeFieldTypeTest() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }

    @Override
    @Disabled
    public void changeFromSerialToClob() {
        //TODO: don't use serial in this test
    }

    @Override
    @Disabled
    public void changeScale() {
        //TODO: fix scale for sqlite
    }

    @Override
    @Disabled
    public void changePrecision() {
        //TODO: fix precision for sqlite
    }

}

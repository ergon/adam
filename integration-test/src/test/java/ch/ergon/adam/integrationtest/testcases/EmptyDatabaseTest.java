package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.schema.Schema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class EmptyDatabaseTest extends AbstractDbTestBase {

    public EmptyDatabaseTest(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void testEmptyDb() {
        DummySink dummySink = dumpToDummySink(getTargetDbSource());
        Schema schema = dummySink.getTargetSchema();
        assertNotNull(schema);
        assertTrue(schema.getTables().isEmpty());
        assertTrue(schema.getViews().isEmpty());
        assertTrue(schema.getEnums().isEmpty());
    }

    @Test
    public void testEmptyDbToYml() {
        String yml = targetToYml();
        assertThat(yml, is("--- []\n--- []\n"));
    }
}

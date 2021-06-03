package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.AssertAnyChangeStrategy;
import ch.ergon.adam.integrationtest.DummySink;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.core.db.SchemaDiffExtractor;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Sequence;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

public abstract class SequenceTests extends AbstractDbTestBase {

    public SequenceTests(TestDbUrlProvider testDbUrlProvider) {
        super(testDbUrlProvider);
    }

    @Test
    public void minmalSequenceNoChange() throws Exception {

        // Setup
        // use empty DB as basis
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();
        // create sequence via migration
        schema.setSequences(asList(new Sequence("miminal_sequence")));
        migrateTargetWithSchema(schema);
        dummySink = targetToDummy();
        schema = dummySink.getTargetSchema();

        // reset to minimal sequence
        schema.setSequences(asList(new Sequence("miminal_sequence")));
        dummySink = targetToDummy();
        Schema sourceSchema = dummySink.getTargetSchema();
        SchemaDiffExtractor diffExtractor = new SchemaDiffExtractor(sourceSchema, schema);

        // validate no migration
        diffExtractor.process(new AssertAnyChangeStrategy());
    }

}

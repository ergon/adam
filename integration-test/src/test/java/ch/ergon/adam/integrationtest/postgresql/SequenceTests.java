package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Sequence;
import ch.ergon.adam.integrationtest.DummySink;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SequenceTests extends AbstractPostgresqlTestBase {

    private static final String CREATE_COMPLEX_SEQUENCE_SQL =
            "CREATE SEQUENCE test_sequence INCREMENT 2 MINVALUE 10 MAXVALUE 100 START WITH 30";

    private static final String CREATE_SIMPLE_SEQUENCE_SQL =
        "CREATE SEQUENCE test_sequence";

    @Test
    public void testCreateSimpleSequence() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_SIMPLE_SEQUENCE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Verify
        Sequence sequence = schema.getSequence("test_sequence");
        assertNotNull(sequence);
        assertThat(sequence.getIncrement(), is(1L));
        assertThat(sequence.getMinValue(), is(1L));
        assertThat(sequence.getMaxValue(), is(Long.MAX_VALUE));
        assertThat(sequence.getStartValue(), is(1L));
    }

    @Test
    public void testCreateComplexSequence() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_COMPLEX_SEQUENCE_SQL);
        sourceToTarget();
        DummySink dummySink = targetToDummy();
        Schema schema = dummySink.getTargetSchema();

        // Verify
        Sequence sequence = schema.getSequence("test_sequence");
        assertNotNull(sequence);
        assertThat(sequence.getIncrement(), is(2L));
        assertThat(sequence.getMinValue(), is(10L));
        assertThat(sequence.getMaxValue(), is(100L));
        assertThat(sequence.getStartValue(), is(30L));
    }
}

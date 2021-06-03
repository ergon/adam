package ch.ergon.adam.core.prepost;

import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.core.filetree.TraverserFile;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MigrationStepExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MigrationStepExecutor.class);
    private final MigrationScriptProvider migrationScriptProvider;
    private final SqlExecutor sqlExecutor;

    public MigrationStepExecutor(MigrationScriptProvider migrationScriptProvider, SqlExecutor sqlExecutor) {
        this.migrationScriptProvider = migrationScriptProvider;
        this.sqlExecutor = sqlExecutor;
    }


    public void executeStep(MigrationStep step) {
        List<TraverserFile> scripts = migrationScriptProvider.getMigrationScripts(step);
        if (!scripts.isEmpty()) {
            logger.info("Executing migration scripts for step [" + step.name() + "].");
        }
        try {
            scripts.forEach(this::executeScript);
        } catch (Exception e) {
            sqlExecutor.rollback();
            throw e;
        }
    }

    private void executeScript(TraverserFile file) {
        logger.info("Executing migration script [" + file.getName() + "].");
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), UTF_8)) {
            String script = CharStreams.toString(reader);
            sqlExecutor.executeScript(script);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read migration script [" + file.getName() + "]");
        }
    }
}

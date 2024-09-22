package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.helper.FileHelper;
import ch.ergon.adam.core.prepost.GitVersionTree;
import ch.ergon.adam.core.prepost.MigrationScriptProvider;
import ch.ergon.adam.core.prepost.MigrationStepExecutor;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.List;

import static ch.ergon.adam.core.Adam.*;
import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_ONCE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigrationStepExecutorTest extends AbstractPostgresqlTestBase {

    @Test
    public void ExecuteScriptsTest() throws Exception {

        Path repoBase = getRepoBase();
        Path testDataBase = repoBase.resolve("integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE);

        String targetUrl = getTargetDbUrl();

        try (
            InputStream gitHistoryInputStream = new FileInputStream(testDataBase.resolve(HISTORY_FILE_NAME).toFile());
            SqlExecutor sqlExecutor = SourceAndSinkFactory.getInstance().getSqlExecutor(targetUrl)
        ) {
            GitVersionTree versionTree = new GitVersionTree(gitHistoryInputStream);
            List<String> versions = versionTree.getVersionsBetween("2", "4");
            DirectoryTraverser traverser = new DirectoryTraverser(testDataBase.resolve("scripts"));
            MigrationScriptProvider migrationScriptProvider = new MigrationScriptProvider(traverser, versions);
            MigrationStepExecutor executor = new MigrationStepExecutor(migrationScriptProvider, sqlExecutor);
            executor.executeStep(PREMIGRATION_ONCE);
        }

        // Data present?
        ResultSet result = getTargetDbConnection().createStatement().executeQuery("select sum(id) from another_table");
        assertTrue(result.next());
        assertThat(result.getInt(1), is(2));
    }


    private Path getRepoBase() {
        Path gitFolder = FileHelper.getRepoBase();
        assertNotNull(gitFolder);
        return gitFolder;
    }
}

package ch.ergon.adam.core.prepost;

import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.filetree.TraverserFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_ALWAYS;
import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_ONCE;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MigrationScriptProviderTest extends PrePostTestBase {

    @Test
    public void testGetMigrationScriptsAll() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("41953516b3803a2d9fc5885461faf1ee706a7f6e", "2bc64fda1aca83d9b55dd5cf0ed272978c59c34e");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_with_commit.sql", "script_without_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsAllReverse() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("2bc64fda1aca83d9b55dd5cf0ed272978c59c34e", "41953516b3803a2d9fc5885461faf1ee706a7f6e");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_without_commit.sql", "script_with_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsOnlyWithoutCommit() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("2bc64fda1aca83d9b55dd5cf0ed272978c59c34e");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_without_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsAlways() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("41953516b3803a2d9fc5885461faf1ee706a7f6e", "2bc64fda1aca83d9b55dd5cf0ed272978c59c34e");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ALWAYS);
        assertScriptOrder(scripts, newArrayList("scriptA.sql", "scriptB.sql"));
    }


    private void assertScriptOrder(List<TraverserFile> scripts, List<String> expectedList) {
        List<String> fileNames = scripts.stream().map(TraverserFile::getName).collect(toList());
        assertEquals(expectedList, fileNames);
    }
}

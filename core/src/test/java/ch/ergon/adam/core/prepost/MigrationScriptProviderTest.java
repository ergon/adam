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
        List<String> versions = newArrayList("37b8bb76211282b2fa7b202a3a774fcd50a5de06", "b79b51616049d7152d7ba96660e3dd8160825401");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_without_commit.sql", "script_with_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsAllReverse() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("b79b51616049d7152d7ba96660e3dd8160825401", "37b8bb76211282b2fa7b202a3a774fcd50a5de06");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_with_commit.sql", "script_without_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsOnlyWithoutCommit() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("37b8bb76211282b2fa7b202a3a774fcd50a5de06");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ONCE);
        assertScriptOrder(scripts, newArrayList("script_without_commit.sql"));
    }

    @Test
    public void testGetMigrationScriptsAlways() {
        Path gitFolder = getRepoBase();
        Path scriptPath = gitFolder.resolve("core/src/test/resources/test_scripts");
        List<String> versions = newArrayList("b79b51616049d7152d7ba96660e3dd8160825401", "37b8bb76211282b2fa7b202a3a774fcd50a5de06");
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(scriptPath), gitFolder, versions);
        List<TraverserFile> scripts = scriptProvider.getMigrationScripts(PREMIGRATION_ALWAYS);
        assertScriptOrder(scripts, newArrayList("scriptA.sql", "scriptB.sql"));
    }


    private void assertScriptOrder(List<TraverserFile> scripts, List<String> expectedList) {
        List<String> fileNames = scripts.stream().map(TraverserFile::getName).collect(toList());
        assertEquals(expectedList, fileNames);
    }
}

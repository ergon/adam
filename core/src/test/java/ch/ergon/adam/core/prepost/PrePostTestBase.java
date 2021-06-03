package ch.ergon.adam.core.prepost;

import ch.ergon.adam.core.helper.FileHelper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PrePostTestBase {

    protected Path getRepoBase() {
        Path gitFolder = FileHelper.getRepoBase();
        assertNotNull(gitFolder);
        return gitFolder;
    }
}

package ch.ergon.adam.core.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.stream;
import static java.util.Comparator.reverseOrder;

public class FileHelper {

    public static void deleteFolderRecursively(Path folder) throws IOException {
        Files.walk(folder)
            .map(Path::toFile)
            .sorted(reverseOrder())
            .forEach(File::delete);
    }

    public static Path getRepoBase() {
        Path gitFolder = Paths.get(System.getProperty("user.dir"));
        while (gitFolder != null && !isGitRepo(gitFolder)) {
            gitFolder = gitFolder.getParent();
        }
        return gitFolder;
    }

    private static boolean isGitRepo(Path currentFolder) {
        return stream(currentFolder.toFile().listFiles()).anyMatch(file -> file.getName().equals(".git"));
    }

}

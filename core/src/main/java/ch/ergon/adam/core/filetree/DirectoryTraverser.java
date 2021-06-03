package ch.ergon.adam.core.filetree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class DirectoryTraverser implements FileTreeTraverser {

    private final File path;

    public DirectoryTraverser(Path path) {
        this.path = path.toFile();
    }

    @Override
    public InputStream openFile(String fileName) throws IOException {
        File file = new File(path, fileName);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    @Override
    public List<String> getFileNames() {
        if (!path.exists()) {
            return newArrayList();
        }
        return stream(path.listFiles()).map(File::getName).sorted().collect(toList());
    }

    @Override
    public FileTreeTraverser cd(String subPath) {
        File subFile = new File(path, subPath);
        if (!subFile.exists() || !subFile.isDirectory()) {
            return null;
        }
        return new DirectoryTraverser(subFile.toPath());
    }

    @Override
    public String toString() {
        return path.getAbsolutePath();
    }

    public File getFile(String name) {
        return new File(path, name);
    }
}

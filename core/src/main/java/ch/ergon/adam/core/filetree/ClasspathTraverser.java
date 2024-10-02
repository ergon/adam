package ch.ergon.adam.core.filetree;

import ch.ergon.adam.core.reflection.ReflectionHelper;

import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ClasspathTraverser implements FileTreeTraverser {

    private final String path;

    public ClasspathTraverser(String path) {
        if (path.endsWith("/")) {
            this.path = path;
        } else {
            this.path = path + "/";
        }
    }

    @Override
    public InputStream openFile(String fileName) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(path + fileName);
    }

    @Override
    public List<String> getFileNames() {
        return ReflectionHelper.findAllRessourcesForPath(path).stream()
            .map(name -> name.replaceFirst(path, ""))
            .sorted()
            .collect(toList());
    }

    @Override
    public FileTreeTraverser cd(String subPath) {
        if (!getFileNames().stream().anyMatch(entry -> entry.equals(subPath) || entry.startsWith(subPath + "/"))) {
            return null;
        }
        return new ClasspathTraverser(path + subPath);
    }

    @Override
    public String toString() {
        return "classpath:" + path;
    }
}

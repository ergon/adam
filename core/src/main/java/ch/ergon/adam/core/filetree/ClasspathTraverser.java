package ch.ergon.adam.core.filetree;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ClasspathTraverser implements FileTreeTraverser {

    private final Reflections reflections;
    private final String path;

    public ClasspathTraverser(String path) {
        if (path.endsWith("/")) {
            this.path = path;
        } else {
            this.path = path + "/";
        }
        reflections = new Reflections(this.path, new ResourcesScanner());
    }

    @Override
    public InputStream openFile(String fileName) {
        Set<String> resources = reflections.getResources(name -> name.equals(fileName));
        if (resources.isEmpty()) {
            return null;
        }
        if (resources.size() > 1) {
            throw new RuntimeException("Found multiple resources with name [" + fileName + "].");
        }
        String filePath = resources.iterator().next();
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

    @Override
    public List<String> getFileNames() {
        return reflections.getResources(name -> true).stream()
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

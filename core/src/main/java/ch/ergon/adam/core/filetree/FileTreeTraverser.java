package ch.ergon.adam.core.filetree;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface FileTreeTraverser {

    InputStream openFile(String fileName) throws IOException;

    List<String> getFileNames();

    default List<TraverserFile> getFiles() {
        return getFileNames().stream()
            .map(fileName -> new TraverserFile(this, fileName))
            .collect(toList());
    }

    FileTreeTraverser cd(String subPath);
}

package ch.ergon.adam.core.filetree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TraverserFile {

    private final FileTreeTraverser traverser;

    public String getName() {
        return name;
    }

    private final String name;

    public TraverserFile(FileTreeTraverser traverser, String name) {
        this.name = name;
        this.traverser = traverser;
    }

    public InputStream getInputStream() throws IOException {
        return traverser.openFile(name);
    }

    public File getFile() {
        if (traverser instanceof DirectoryTraverser) {
            return ((DirectoryTraverser) traverser).getFile(name);
        }
        return null;
    }
}

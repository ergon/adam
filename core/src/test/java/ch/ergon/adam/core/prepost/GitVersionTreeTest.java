package ch.ergon.adam.core.prepost;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitVersionTreeTest extends PrePostTestBase {

    @Test
    public void testGitVersionTreeFromRepo() throws IOException {
        Path gitFolder = getRepoBase();
        GitVersionTree versionTree = new GitVersionTree(gitFolder);
        List<String> versions = versionTree.getVersionsBetween("6a6f6c067ac8e748091b3fa325e120c94636ec33", "14bf4c64eec1b4da0b5e21f891f02296d156d7eb");
        List<String> expectedVersions = newArrayList("dc19a3e126de33c58e88172f6707d99d9398cf33", "9c1a3ab33424b91874a6eaa3120a2a67bb6e83eb", "de3564d8a0c4d0cf5f469631da2cfd2bcdc4420d", "13232d92ef26a4cc252bc7d49888bed550dd0663", "14bf4c64eec1b4da0b5e21f891f02296d156d7eb");
        assertEquals(expectedVersions, versions);
    }

    @Test
    public void testGitVersionTreeFromRepoUnrelatedBranches() throws IOException {
        Path gitFolder = getRepoBase();
        GitVersionTree versionTree = new GitVersionTree(gitFolder);
        List<String> versions = versionTree.getVersionsBetween("6a6f6c067ac8e748091b3fa325e120c94636ec33", "f40622b3642be2368cb225b200e9f35caeac3800");
        List<String> expectedVersions = newArrayList("f40622b3642be2368cb225b200e9f35caeac3800");
        assertEquals(expectedVersions, versions);
    }
}

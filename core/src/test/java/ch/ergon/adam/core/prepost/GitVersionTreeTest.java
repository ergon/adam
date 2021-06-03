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
        List<String> versions = versionTree.getVersionsBetween("37b8bb76211282b2fa7b202a3a774fcd50a5de06", "4ccf2191b455d1f147d8522e92514d481d7eaa77");
        List<String> expectedVersions = newArrayList("e68f2ab4b5ca6930ac6cb359663762765f9cfdea", "4ccf2191b455d1f147d8522e92514d481d7eaa77");
        assertEquals(expectedVersions, versions);
    }

    @Test
    public void testGitVersionTreeFromRepoUnrelatedBranches() throws IOException {
        Path gitFolder = getRepoBase();
        GitVersionTree versionTree = new GitVersionTree(gitFolder);
        List<String> versions = versionTree.getVersionsBetween("226c98b701585c6339d795f263d4f846afcb7e13", "4ccf2191b455d1f147d8522e92514d481d7eaa77");
        List<String> expectedVersions = newArrayList("e68f2ab4b5ca6930ac6cb359663762765f9cfdea", "4ccf2191b455d1f147d8522e92514d481d7eaa77");
        assertEquals(expectedVersions, versions);
    }
}

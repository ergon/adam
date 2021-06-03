package ch.ergon.adam.core.prepost;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GitVersionTree {

    Map<String, Collection<String>> gitCommitsWithAncestor = new HashMap<>();

    public GitVersionTree(Path path) throws IOException {
        loadFromGitRepo(path.toFile());
    }

    public GitVersionTree(InputStream is) throws IOException {
        loadFromHistoryFile(is);
    }

    private void loadFromHistoryFile(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            br.lines().forEach(line -> {
                String[] parts = line.split(" ", 2);
                String[] parents = parts.length == 1 || parts[1].isEmpty() ? new String[0] : parts[1].split(" ");
                gitCommitsWithAncestor.put(parts[0], newArrayList(parents));
            });
        }
    }

    private void loadFromGitRepo(File gitRepo) throws IOException {
        try {
            Git git = Git.open(gitRepo);
            git.log().all().call().forEach(commit -> {
                String id = commit.getId().name();
                if (!gitCommitsWithAncestor.containsKey(id)) {
                    List<String> parents = stream(commit.getParents()).map(parentCommit -> parentCommit.getId().name()).collect(toList());
                    gitCommitsWithAncestor.put(id, parents);
                }

            });
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    public void writeToFile(OutputStream outputStream) {
        gitCommitsWithAncestor.entrySet().forEach(entry ->
            {
                String parents = entry.getValue().stream().collect(joining(" "));
                try {
                    outputStream.write(format("%s %s\n", entry.getKey(), parents).getBytes(UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

    /**
     * @param fromVersion the source version
     * @param toVersion   the target version
     * @return the versions part of target version tree but not source version tree. Includes toVersion but not fromVersion
     */
    public List<String> getVersionsBetween(String fromVersion, String toVersion) {
        Collection<String> oldCommits = getCommitsContainedIn(fromVersion);
        Collection<String> newCommits = getCommitsContainedIn(toVersion);
        newCommits.removeAll(oldCommits);
        List<String> sortedCommits = sortCommit(newCommits);
        return sortedCommits;
    }

    private Collection<String> getCommitsContainedIn(String commitId) {
        Collection<String> commits = new HashSet<>();
        Queue<String> commitsToFollow = new LinkedList<>();
        commitsToFollow.add(commitId);
        while (!commitsToFollow.isEmpty()) {
            String commit = commitsToFollow.poll();
            if (commits.contains(commit)) {
                continue;
            }
            commits.add(commit);
            commitsToFollow.addAll(gitCommitsWithAncestor.get(commit));
        }
        return commits;
    }

    private List<String> sortCommit(Collection<String> commits) {
        Map<String, Integer> commitsWithInDegrees = commits.stream().collect(toLinkedMap(identity(), commit -> 0));
        commits.stream()
            .flatMap(commit -> gitCommitsWithAncestor.get(commit).stream())
            .filter(commitsWithInDegrees::containsKey)
            .forEach(ancestor -> {
                commitsWithInDegrees.replace(ancestor, commitsWithInDegrees.get(ancestor) + 1);
            });

        List<String> orderedCommits = new ArrayList<>();

        while (!commitsWithInDegrees.isEmpty()) {
            List<String> commitsWithZeroInDegree = commitsWithInDegrees.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(toList());


            if (commitsWithZeroInDegree.isEmpty()) {
                throw new RuntimeException("No commits found with zero in degree.");
            }

            commitsWithZeroInDegree.forEach(
                commitWithZeroInDegree -> {
                    commitsWithInDegrees.remove(commitWithZeroInDegree);
                    gitCommitsWithAncestor.get(commitWithZeroInDegree).forEach(ancestor -> {
                        if (commitsWithInDegrees.containsKey(ancestor)) {
                            commitsWithInDegrees.replace(ancestor, commitsWithInDegrees.get(ancestor) - 1);
                        }
                    });
                }
            );

            orderedCommits.addAll(commitsWithZeroInDegree);
        }
        return reverse(orderedCommits);
    }

    public boolean isKnownVersion(String commitId) {
        return gitCommitsWithAncestor.containsKey(commitId);
    }

    public boolean isAncestor(String ancestorCommit, String referenceCommit) {
        return getCommitsContainedIn(referenceCommit).contains(ancestorCommit);
    }
}

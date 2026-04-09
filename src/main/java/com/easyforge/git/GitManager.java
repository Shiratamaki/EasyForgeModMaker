package com.easyforge.git;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GitManager {
    private final File projectDir;
    private Git git;
    private Repository repository;

    public GitManager(File projectDir) {
        this.projectDir = projectDir;
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            repository = builder.setWorkTree(projectDir).build();
            git = new Git(repository);
        } catch (IOException e) {
            git = null;
            repository = null;
        }
    }

    public boolean isGitRepo() {
        return git != null && repository != null;
    }

    public Status getStatus() {
        if (!isGitRepo()) return null;
        try {
            return git.status().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCurrentBranch() {
        if (!isGitRepo()) return null;
        try {
            return repository.getBranch();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getAllBranches() {
        List<String> branches = new ArrayList<>();
        if (!isGitRepo()) return branches;
        try {
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                String name = ref.getName().replace("refs/heads/", "");
                branches.add(name);
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return branches;
    }

    public boolean init() {
        try {
            git = Git.init().setDirectory(projectDir).call();
            repository = git.getRepository();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static GitManager cloneRepository(String remoteUrl, File localDir) {
        try {
            Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localDir)
                    .call();
            return new GitManager(localDir);
        } catch (GitAPIException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addAll() {
        if (!isGitRepo()) return false;
        try {
            git.add().addFilepattern(".").call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addFile(String filePattern) {
        if (!isGitRepo()) return false;
        try {
            git.add().addFilepattern(filePattern).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean commit(String message, String authorName, String authorEmail) {
        if (!isGitRepo()) return false;
        try {
            CommitCommand commit = git.commit().setMessage(message);
            if (authorName != null && !authorName.isEmpty() && authorEmail != null && !authorEmail.isEmpty()) {
                commit.setAuthor(authorName, authorEmail);
                commit.setCommitter(authorName, authorEmail);
            }
            commit.call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean push(String remote, String branch, String username, String password) {
        if (!isGitRepo()) return false;
        try {
            PushCommand push = git.push().setRemote(remote);
            if (branch != null && !branch.isEmpty()) {
                push.setRefSpecs(new RefSpec("refs/heads/" + branch + ":refs/heads/" + branch));
            }
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            push.call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean pull(String remote, String branch, String username, String password) {
        if (!isGitRepo()) return false;
        try {
            PullCommand pull = git.pull();
            if (remote != null && !remote.isEmpty()) pull.setRemote(remote);
            if (branch != null && !branch.isEmpty()) pull.setRemoteBranchName(branch);
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                pull.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            pull.call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createBranch(String branchName) {
        if (!isGitRepo()) return false;
        try {
            git.branchCreate().setName(branchName).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkoutBranch(String branchName) {
        if (!isGitRepo()) return false;
        try {
            git.checkout().setName(branchName).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkoutNewBranch(String branchName) {
        if (!isGitRepo()) return false;
        try {
            git.checkout().setCreateBranch(true).setName(branchName).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBranch(String branchName) {
        if (!isGitRepo()) return false;
        try {
            git.branchDelete().setBranchNames(branchName).setForce(true).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createTag(String tagName, String message) {
        if (!isGitRepo()) return false;
        try {
            git.tag().setName(tagName).setMessage(message).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        if (!isGitRepo()) return tags;
        try {
            List<Ref> refs = git.tagList().call();
            for (Ref ref : refs) {
                String name = ref.getName().replace("refs/tags/", "");
                tags.add(name);
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public boolean addRemote(String remoteName, String remoteUrl) {
        if (!isGitRepo()) return false;
        try {
            git.remoteAdd().setName(remoteName).setUri(new URIish(remoteUrl)).call();
            return true;
        } catch (GitAPIException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeRemote(String remoteName) {
        if (!isGitRepo()) return false;
        try {
            git.remoteRemove().setRemoteName(remoteName).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getRemotes() {
        List<String> remotes = new ArrayList<>();
        if (!isGitRepo()) return remotes;
        try {
            List<RemoteConfig> configs = git.remoteList().call();
            for (RemoteConfig config : configs) {
                remotes.add(config.getName());
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return remotes;
    }

    public boolean reset(String target) {
        if (!isGitRepo()) return false;
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(target).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 修正 revertCommit 方法：使用 include(RevCommit)
    public boolean revertCommit(String commitId) {
        if (!isGitRepo()) return false;
        try {
            ObjectId id = repository.resolve(commitId);
            if (id == null) return false;
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(id);
                git.revert().include(commit).call();
                return true;
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RevCommit> getCommitLog(int maxCount) {
        List<RevCommit> commits = new ArrayList<>();
        if (!isGitRepo()) return commits;
        try {
            Iterable<RevCommit> log = git.log().setMaxCount(maxCount).call();
            log.forEach(commits::add);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return commits;
    }

    public List<String> getChangedFilesInCommit(String commitId) {
        List<String> files = new ArrayList<>();
        if (!isGitRepo()) return files;
        try {
            ObjectId commitIdObj = repository.resolve(commitId);
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitIdObj);
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        files.add(treeWalk.getPathString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public List<String> getDiff() {
        List<String> diffLines = new ArrayList<>();
        if (!isGitRepo()) return diffLines;
        try {
            git.diff().call().forEach(diffEntry -> {
                diffLines.add(diffEntry.getNewPath() + " (" + diffEntry.getChangeType().name() + ")");
            });
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return diffLines;
    }

    public boolean clean() {
        if (!isGitRepo()) return false;
        try {
            git.clean().setCleanDirectories(true).call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (git != null) git.close();
        if (repository != null) repository.close();
    }
}
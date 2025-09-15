package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.model.CommandResult;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitService {

    private final SSHService sshService;

    public void cloneRepository(String serverName, String repoUrl, String targetPath, String branch) {
        String command = branch != null ?
                String.format("git clone -b %s %s %s", branch, repoUrl, targetPath) :
                String.format("git clone %s %s", repoUrl, targetPath);

        CommandResult result = sshService.executeCommand(serverName, command, false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to clone repository: " + result.getError());
        }
    }

    public String pullChanges(String serverName, String repoPath) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git pull", repoPath), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to pull changes: " + result.getError());
        }
        return result.getOutput();
    }

    public void checkout(String serverName, String repoPath, String branchOrTag) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git checkout %s", repoPath, branchOrTag), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to checkout: " + result.getError());
        }
    }

    public String getStatus(String serverName, String repoPath) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git status", repoPath), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to get status: " + result.getError());
        }
        return result.getOutput();
    }

    public List<String> listBranches(String serverName, String repoPath) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git branch -a", repoPath), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to list branches: " + result.getError());
        }
        return Arrays.asList(result.getOutput().split("\n"));
    }

    public void addAll(String serverName, String repoPath) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git add .", repoPath), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to add files: " + result.getError());
        }
    }

    public void commit(String serverName, String repoPath, String message) {
        CommandResult result = sshService.executeCommand(serverName,
                String.format("cd %s && git commit -m '%s'", repoPath, message.replace("'", "'\\'''")), false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to commit: " + result.getError());
        }
    }

    public void push(String serverName, String repoPath, String branch) {
        String command = branch != null ?
                String.format("cd %s && git push origin %s", repoPath, branch) :
                String.format("cd %s && git push", repoPath);

        CommandResult result = sshService.executeCommand(serverName, command, false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to push: " + result.getError());
        }
    }
}
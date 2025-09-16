package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.exception.*;
import net.alishahidi.mcpconductor.model.CommandResult;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitService {

    private final SSHService sshService;

    private static final Pattern GIT_URL_PATTERN = Pattern.compile(
            "^(https?://|git@|ssh://|git://|file://).+\\.git$|^(https?://|git@|ssh://|git://|file://).+$"
    );

    @Retryable(
            value = {GitOperationException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000)
    )
    public void cloneRepository(String serverName, String repoUrl, String targetPath, String branch) {
        log.info("Cloning repository: {} to: {} on server: {}", repoUrl, targetPath, serverName);

        // Validate repository URL
        if (!isValidGitUrl(repoUrl)) {
            throw new ValidationException("repoUrl", repoUrl, "Invalid Git repository URL");
        }

        // Check if target path already exists
        CommandResult pathCheck = sshService.executeCommand(
                serverName,
                String.format("test -e '%s' && echo 'exists' || echo 'not_exists'", targetPath),
                false
        );

        if (pathCheck.getOutput().trim().equals("exists")) {
            throw new GitOperationException(
                    "Target path already exists",
                    repoUrl,
                    GitOperationException.GitOperation.CLONE
            );
        }

        String command = branch != null ?
                String.format("git clone -b %s '%s' '%s'", branch, repoUrl, targetPath) :
                String.format("git clone '%s' '%s'", repoUrl, targetPath);

        CommandResult result = sshService.executeCommand(serverName, command, false);

        if (!result.isSuccess()) {
            String error = result.getError();

            if (error.contains("Repository not found") ||
                    error.contains("Could not read from remote")) {
                throw new ResourceNotFoundException("Git repository", repoUrl);
            }

            if (error.contains("Permission denied") ||
                    error.contains("Authentication failed")) {
                throw new GitOperationException(
                        "Authentication failed for repository",
                        repoUrl,
                        branch,
                        GitOperationException.GitOperation.CLONE,
                        null
                );
            }

            if (error.contains("Remote branch") && error.contains("not found")) {
                throw new ResourceNotFoundException("Branch", branch);
            }

            throw new GitOperationException(
                    "Failed to clone repository: " + error,
                    repoUrl,
                    branch,
                    GitOperationException.GitOperation.CLONE,
                    null
            );
        }

        log.info("Repository cloned successfully to: {}", targetPath);
    }

    public String pullChanges(String serverName, String repoPath) {
        log.info("Pulling changes in: {} on server: {}", repoPath, serverName);

        // Verify it's a git repository
        validateGitRepository(serverName, repoPath);

        // Check for uncommitted changes
        CommandResult statusCheck = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git status --porcelain", repoPath),
                false
        );

        if (!statusCheck.getOutput().trim().isEmpty()) {
            log.warn("Repository has uncommitted changes: {}", repoPath);
        }

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git pull", repoPath),
                false
        );

        if (!result.isSuccess()) {
            String error = result.getError();

            if (error.contains("Merge conflict")) {
                throw new GitOperationException(
                        "Merge conflicts detected. Manual resolution required",
                        repoPath,
                        GitOperationException.GitOperation.PULL
                );
            }

            if (error.contains("Authentication")) {
                throw new GitOperationException(
                        "Authentication failed",
                        repoPath,
                        GitOperationException.GitOperation.PULL
                );
            }

            throw new GitOperationException(
                    "Failed to pull changes: " + error,
                    repoPath,
                    GitOperationException.GitOperation.PULL
            );
        }

        return result.getOutput();
    }

    public void checkout(String serverName, String repoPath, String branchOrTag) {
        log.info("Checking out: {} in: {} on server: {}", branchOrTag, repoPath, serverName);

        if (branchOrTag == null || branchOrTag.trim().isEmpty()) {
            throw new ValidationException("branchOrTag", branchOrTag, "Branch or tag cannot be empty");
        }

        validateGitRepository(serverName, repoPath);

        // Fetch latest references
        sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git fetch --all", repoPath),
                false
        );

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git checkout '%s'", repoPath, branchOrTag),
                false
        );

        if (!result.isSuccess()) {
            String error = result.getError();

            if (error.contains("did not match any") ||
                    error.contains("pathspec")) {
                throw new ResourceNotFoundException("Branch/Tag", branchOrTag);
            }

            if (error.contains("Your local changes")) {
                throw new GitOperationException(
                        "Cannot checkout: uncommitted changes present",
                        repoPath,
                        branchOrTag,
                        GitOperationException.GitOperation.CHECKOUT,
                        null
                );
            }

            throw new GitOperationException(
                    "Failed to checkout: " + error,
                    repoPath,
                    branchOrTag,
                    GitOperationException.GitOperation.CHECKOUT,
                    null
            );
        }

        log.info("Successfully checked out: {}", branchOrTag);
    }

    public String getStatus(String serverName, String repoPath) {
        log.info("Getting status for: {} on server: {}", repoPath, serverName);

        validateGitRepository(serverName, repoPath);

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git status", repoPath),
                false
        );

        if (!result.isSuccess()) {
            throw new GitOperationException(
                    "Failed to get status: " + result.getError(),
                    repoPath,
                    GitOperationException.GitOperation.STATUS
            );
        }

        return result.getOutput();
    }

    public List<String> listBranches(String serverName, String repoPath) {
        log.info("Listing branches in: {} on server: {}", repoPath, serverName);

        validateGitRepository(serverName, repoPath);

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git branch -a", repoPath),
                false
        );

        if (!result.isSuccess()) {
            throw new GitOperationException(
                    "Failed to list branches: " + result.getError(),
                    repoPath,
                    GitOperationException.GitOperation.FETCH
            );
        }

        return Arrays.stream(result.getOutput().split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    public void addAll(String serverName, String repoPath) {
        log.info("Adding all files in: {} on server: {}", repoPath, serverName);

        validateGitRepository(serverName, repoPath);

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git add .", repoPath),
                false
        );

        if (!result.isSuccess()) {
            throw new GitOperationException(
                    "Failed to add files: " + result.getError(),
                    repoPath,
                    GitOperationException.GitOperation.COMMIT
            );
        }
    }

    public void commit(String serverName, String repoPath, String message) {
        log.info("Committing changes in: {} on server: {}", repoPath, serverName);

        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("message", message, "Commit message cannot be empty");
        }

        validateGitRepository(serverName, repoPath);

        // Check if there are changes to commit
        CommandResult statusCheck = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git diff --cached --quiet", repoPath),
                false
        );

        if (statusCheck.getExitCode() == 0) {
            throw new GitOperationException(
                    "No changes staged for commit",
                    repoPath,
                    GitOperationException.GitOperation.COMMIT
            );
        }

        String escapedMessage = message.replace("'", "'\\''");
        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cd '%s' && git commit -m '%s'", repoPath, escapedMessage),
                false
        );

        if (!result.isSuccess()) {
            throw new GitOperationException(
                    "Failed to commit: " + result.getError(),
                    repoPath,
                    GitOperationException.GitOperation.COMMIT
            );
        }

        log.info("Changes committed successfully");
    }

    public void push(String serverName, String repoPath, String branch) {
        log.info("Pushing changes from: {} on server: {}", repoPath, serverName);

        validateGitRepository(serverName, repoPath);

        String command = branch != null ?
                String.format("cd '%s' && git push origin '%s'", repoPath, branch) :
                String.format("cd '%s' && git push", repoPath);

        CommandResult result = sshService.executeCommand(serverName, command, false);

        if (!result.isSuccess()) {
            String error = result.getError();

            if (error.contains("Authentication")) {
                throw new GitOperationException(
                        "Authentication failed for push",
                        repoPath,
                        branch,
                        GitOperationException.GitOperation.PUSH,
                        null
                );
            }

            if (error.contains("rejected") || error.contains("non-fast-forward")) {
                throw new GitOperationException(
                        "Push rejected: pull remote changes first",
                        repoPath,
                        branch,
                        GitOperationException.GitOperation.PUSH,
                        null
                );
            }

            throw new GitOperationException(
                    "Failed to push: " + error,
                    repoPath,
                    branch,
                    GitOperationException.GitOperation.PUSH,
                    null
            );
        }

        log.info("Changes pushed successfully");
    }

    private void validateGitRepository(String serverName, String repoPath) {
        CommandResult gitCheck = sshService.executeCommand(
                serverName,
                String.format("test -d '%s/.git' && echo 'git' || echo 'not_git'", repoPath),
                false
        );

        if (gitCheck.getOutput().trim().equals("not_git")) {
            throw new GitOperationException(
                    "Not a git repository",
                    repoPath,
                    GitOperationException.GitOperation.STATUS
            );
        }
    }

    private boolean isValidGitUrl(String url) {
        return url != null && GIT_URL_PATTERN.matcher(url).matches();
    }
}
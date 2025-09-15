package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.GitService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitOperationsTool {

    private final GitService gitService;

    @Tool(name = "clone_repository", description = "Clone a Git repository to a remote server. Essential for deploying applications, setting up development environments, or getting source code onto servers. Supports cloning specific branches for targeted deployments.")
    public String cloneRepository(
            @ToolParam(description = "The Git repository URL to clone (e.g., 'https://github.com/user/repo.git', 'git@github.com:user/repo.git', 'https://gitlab.com/user/project.git'). Supports HTTP/HTTPS and SSH protocols.") String repositoryUrl,
            @ToolParam(description = "The directory path where the repository should be cloned (e.g., '/opt/myapp', '/var/www/website', '/home/user/projects/myproject'). Parent directory must exist.") String targetPath,
            @ToolParam(description = "The specific branch to clone (e.g., 'main', 'develop', 'release/v2.0', 'feature/new-auth'). Use 'main' or 'master' for default branch, or specific branch names for targeted deployments.") String branch,
            @ToolParam(description = "The target server identifier where the repository should be cloned (e.g., 'production', 'staging', 'development', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Cloning repository: {} to: {} on server: {}",
                repositoryUrl, targetPath, serverName);

        gitService.cloneRepository(serverName, repositoryUrl, targetPath, branch);
        return "Repository cloned successfully to: " + targetPath;
    }

    @Tool(name = "git_pull", description = "Pull the latest changes from the remote Git repository. Essential for updating deployed applications, getting latest code changes, and keeping repositories synchronized with upstream changes.")
    public String pullChanges(
            @ToolParam(description = "The path to the local Git repository directory (e.g., '/opt/myapp', '/var/www/website', '/home/user/project'). Must be an existing Git repository directory.") String repositoryPath,
            @ToolParam(description = "The target server identifier where the repository is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Pulling changes in: {} on server: {}", repositoryPath, serverName);

        String result = gitService.pullChanges(serverName, repositoryPath);
        return "Pull completed: " + result;
    }

    @Tool(name = "git_checkout", description = "Checkout a specific branch or tag in the Git repository. Perfect for switching between versions, deploying specific releases, or working with different feature branches. Changes the working directory to match the specified branch/tag.")
    public String checkout(
            @ToolParam(description = "The path to the local Git repository directory (e.g., '/opt/myapp', '/var/www/website', '/home/user/project'). Must be an existing Git repository directory.") String repositoryPath,
            @ToolParam(description = "The branch name or tag to checkout (e.g., 'main', 'develop', 'v1.2.3', 'feature/auth', 'hotfix/security'). Use branch names for ongoing development or tags for specific releases.") String branchOrTag,
            @ToolParam(description = "The target server identifier where the repository is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Checking out: {} in: {} on server: {}",
                branchOrTag, repositoryPath, serverName);

        gitService.checkout(serverName, repositoryPath, branchOrTag);
        return "Checked out to: " + branchOrTag;
    }

    @Tool(name = "git_status", description = "Get the current status of the Git repository including modified files, staged changes, and current branch. Essential for understanding repository state, checking for uncommitted changes, and repository health monitoring.")
    public String getStatus(
            @ToolParam(description = "The path to the local Git repository directory (e.g., '/opt/myapp', '/var/www/website', '/home/user/project'). Must be an existing Git repository directory.") String repositoryPath,
            @ToolParam(description = "The target server identifier where the repository is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Getting status for: {} on server: {}", repositoryPath, serverName);
        return gitService.getStatus(serverName, repositoryPath);
    }

    @Tool(name = "git_list_branches", description = "List all branches in the Git repository including local and remote branches. Useful for understanding available branches, planning deployments, and seeing what development branches exist.")
    public List<String> listBranches(
            @ToolParam(description = "The path to the local Git repository directory (e.g., '/opt/myapp', '/var/www/website', '/home/user/project'). Must be an existing Git repository directory.") String repositoryPath,
            @ToolParam(description = "The target server identifier where the repository is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Listing branches in: {} on server: {}", repositoryPath, serverName);
        return gitService.listBranches(serverName, repositoryPath);
    }

    @Tool(name = "git_commit", description = "Add all changes and commit them with a message. Stages all modified files and creates a new commit. Perfect for saving configuration changes, deployment modifications, or any updates made on the server.")
    public String commitChanges(
            @ToolParam(description = "The path to the local Git repository directory (e.g., '/opt/myapp', '/var/www/website', '/home/user/project'). Must be an existing Git repository directory.") String repositoryPath,
            @ToolParam(description = "The commit message describing the changes (e.g., 'Update configuration for production', 'Fix security vulnerability', 'Deploy version 2.1.0'). Should be descriptive and follow commit message conventions.") String message,
            @ToolParam(description = "The target server identifier where the repository is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Committing changes in: {} on server: {}", repositoryPath, serverName);

        gitService.addAll(serverName, repositoryPath);
        gitService.commit(serverName, repositoryPath, message);
        return "Changes committed with message: " + message;
    }
}
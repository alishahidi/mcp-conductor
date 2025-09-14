package net.alishahidi.mcpconductor.tools;

import com.devops.mcp.service.GitService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitOperationsTool {

    private final GitService gitService;

    @Tool(description = "Clone a Git repository")
    public String cloneRepository(String repositoryUrl,
                                  String targetPath,
                                  String branch,
                                  String serverName) {
        log.info("Cloning repository: {} to: {} on server: {}",
                repositoryUrl, targetPath, serverName);

        gitService.cloneRepository(serverName, repositoryUrl, targetPath, branch);
        return "Repository cloned successfully to: " + targetPath;
    }

    @Tool(description = "Pull latest changes from Git repository")
    public String pullChanges(String repositoryPath, String serverName) {
        log.info("Pulling changes in: {} on server: {}", repositoryPath, serverName);

        String result = gitService.pullChanges(serverName, repositoryPath);
        return "Pull completed: " + result;
    }

    @Tool(description = "Checkout a specific branch or tag")
    public String checkout(String repositoryPath, String branchOrTag, String serverName) {
        log.info("Checking out: {} in: {} on server: {}",
                branchOrTag, repositoryPath, serverName);

        gitService.checkout(serverName, repositoryPath, branchOrTag);
        return "Checked out to: " + branchOrTag;
    }

    @Tool(description = "Get Git repository status")
    public String getStatus(String repositoryPath, String serverName) {
        log.info("Getting status for: {} on server: {}", repositoryPath, serverName);
        return gitService.getStatus(serverName, repositoryPath);
    }

    @Tool(description = "List branches in repository")
    public List<String> listBranches(String repositoryPath, String serverName) {
        log.info("Listing branches in: {} on server: {}", repositoryPath, serverName);
        return gitService.listBranches(serverName, repositoryPath);
    }

    @Tool(description = "Add and commit changes")
    public String commitChanges(String repositoryPath,
                                String message,
                                String serverName) {
        log.info("Committing changes in: {} on server: {}", repositoryPath, serverName);

        gitService.addAll(serverName, repositoryPath);
        gitService.commit(serverName, repositoryPath, message);
        return "Changes committed with message: " + message;
    }
}
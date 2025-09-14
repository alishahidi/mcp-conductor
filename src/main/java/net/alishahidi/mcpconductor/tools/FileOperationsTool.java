package net.alishahidi.mcpconductor.tools;

import com.devops.mcp.service.FileService;
import com.devops.mcp.security.PathValidator;
import com.devops.mcp.model.FileOperation;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileOperationsTool {

    private final FileService fileService;
    private final PathValidator pathValidator;

    @Tool(description = "Read content from a file on the server")
    public String readFile(String filePath, String serverName) {
        log.info("Reading file: {} from server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        return fileService.readFile(serverName, filePath);
    }

    @Tool(description = "Write content to a file on the server")
    public String writeFile(String filePath, String content, String serverName) {
        log.info("Writing to file: {} on server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.writeFile(serverName, filePath, content);
        return "File written successfully: " + filePath;
    }

    @Tool(description = "Append content to a file on the server")
    public String appendToFile(String filePath, String content, String serverName) {
        log.info("Appending to file: {} on server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.appendToFile(serverName, filePath, content);
        return "Content appended to file: " + filePath;
    }

    @Tool(description = "Delete a file from the server")
    public String deleteFile(String filePath, String serverName) {
        log.info("Deleting file: {} from server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.deleteFile(serverName, filePath);
        return "File deleted: " + filePath;
    }

    @Tool(description = "List files in a directory")
    public List<String> listFiles(String directoryPath, String serverName) {
        log.info("Listing files in: {} on server: {}", directoryPath, serverName);

        if (!pathValidator.isValidPath(directoryPath)) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        return fileService.listFiles(serverName, directoryPath);
    }

    @Tool(description = "Change file permissions")
    public String changePermissions(String filePath, String permissions, String serverName) {
        log.info("Changing permissions for: {} to: {} on server: {}",
                filePath, permissions, serverName);

        fileService.changePermissions(serverName, filePath, permissions);
        return "Permissions changed for: " + filePath;
    }

    @Tool(description = "Change file ownership")
    public String changeOwnership(String filePath, String owner, String group, String serverName) {
        log.info("Changing ownership for: {} to: {}:{} on server: {}",
                filePath, owner, group, serverName);

        fileService.changeOwnership(serverName, filePath, owner, group);
        return "Ownership changed for: " + filePath;
    }
}
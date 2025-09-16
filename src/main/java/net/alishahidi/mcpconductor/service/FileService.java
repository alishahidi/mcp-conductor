package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.exception.*;
import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.security.PathValidator;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final SSHService sshService;
    private final PathValidator pathValidator;

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Cacheable(value = "file-content", key = "#serverName + ':' + #filePath")
    public String readFile(String serverName, String filePath) {
        log.info("Reading file: {} from server: {}", filePath, serverName);

        // Validate path
        Path path = validatePath(filePath, FileOperationException.OperationType.READ);

        // Check file size first
        CommandResult sizeCheck = sshService.executeCommand(
                serverName,
                String.format("stat -c%%s '%s' 2>/dev/null || echo 0", filePath),
                false
        );

        try {
            long fileSize = Long.parseLong(sizeCheck.getOutput().trim());
            if (fileSize > MAX_FILE_SIZE) {
                throw new FileOperationException(
                        String.format("File too large: %d bytes (max: %d bytes)", fileSize, MAX_FILE_SIZE),
                        path,
                        FileOperationException.OperationType.READ
                );
            }
        } catch (NumberFormatException e) {
            log.warn("Could not determine file size for: {}", filePath);
        }

        // Read the file
        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("cat '%s'", filePath),
                false
        );

        if (!result.isSuccess()) {
            if (result.getError().contains("No such file") ||
                    result.getOutput().contains("No such file")) {
                throw new ResourceNotFoundException("File", filePath);
            }
            if (result.getError().contains("Permission denied")) {
                throw new FileOperationException(
                        "Permission denied",
                        path,
                        FileOperationException.OperationType.READ,
                        serverName,
                        null
                );
            }
            throw new FileOperationException(
                    "Failed to read file: " + result.getError(),
                    path,
                    FileOperationException.OperationType.READ,
                    serverName,
                    null
            );
        }

        return result.getOutput();
    }

    @CacheEvict(value = "file-content", key = "#serverName + ':' + #filePath")
    public void writeFile(String serverName, String filePath, String content) {
        log.info("Writing to file: {} on server: {}", filePath, serverName);

        // Validate inputs
        if (content == null) {
            throw new ValidationException("content", null, "File content cannot be null");
        }

        Path path = validatePath(filePath, FileOperationException.OperationType.WRITE);

        // Check parent directory exists
        String parentDir = Paths.get(filePath).getParent().toString();
        CommandResult dirCheck = sshService.executeCommand(
                serverName,
                String.format("test -d '%s' && echo 'exists' || echo 'not_exists'", parentDir),
                false
        );

        if (dirCheck.getOutput().trim().equals("not_exists")) {
            throw new FileOperationException(
                    "Parent directory does not exist: " + parentDir,
                    path,
                    FileOperationException.OperationType.WRITE,
                    serverName,
                    null
            );
        }

        // Write file with proper escaping
        String escapedContent = content.replace("'", "'\\''");
        String command = String.format("echo '%s' > '%s'", escapedContent, filePath);

        CommandResult result = sshService.executeCommand(serverName, command, true);

        if (!result.isSuccess()) {
            if (result.getError().contains("Permission denied")) {
                throw new FileOperationException(
                        "Permission denied",
                        path,
                        FileOperationException.OperationType.WRITE,
                        serverName,
                        null
                );
            }
            throw new FileOperationException(
                    "Failed to write file: " + result.getError(),
                    path,
                    FileOperationException.OperationType.WRITE,
                    serverName,
                    null
            );
        }

        log.info("File written successfully: {}", filePath);
    }

    @CacheEvict(value = "file-content", key = "#serverName + ':' + #filePath")
    public void appendToFile(String serverName, String filePath, String content) {
        log.info("Appending to file: {} on server: {}", filePath, serverName);

        if (content == null) {
            throw new ValidationException("content", null, "Content to append cannot be null");
        }

        Path path = validatePath(filePath, FileOperationException.OperationType.WRITE);

        // Check if file exists
        CommandResult fileCheck = sshService.executeCommand(
                serverName,
                String.format("test -f '%s' && echo 'exists' || echo 'not_exists'", filePath),
                false
        );

        if (fileCheck.getOutput().trim().equals("not_exists")) {
            throw new ResourceNotFoundException("File", filePath);
        }

        String escapedContent = content.replace("'", "'\\''");
        String command = String.format("echo '%s' >> '%s'", escapedContent, filePath);

        CommandResult result = sshService.executeCommand(serverName, command, true);

        if (!result.isSuccess()) {
            throw new FileOperationException(
                    "Failed to append to file: " + result.getError(),
                    path,
                    FileOperationException.OperationType.WRITE,
                    serverName,
                    null
            );
        }
    }

    @CacheEvict(value = "file-content", key = "#serverName + ':' + #filePath")
    public void deleteFile(String serverName, String filePath) {
        log.info("Deleting file: {} from server: {}", filePath, serverName);

        Path path = validatePath(filePath, FileOperationException.OperationType.DELETE);

        // Check if file exists before deletion
        CommandResult fileCheck = sshService.executeCommand(
                serverName,
                String.format("test -f '%s' && echo 'exists' || echo 'not_exists'", filePath),
                false
        );

        if (fileCheck.getOutput().trim().equals("not_exists")) {
            throw new ResourceNotFoundException("File", filePath);
        }

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("rm -f '%s'", filePath),
                true
        );

        if (!result.isSuccess()) {
            throw new FileOperationException(
                    "Failed to delete file: " + result.getError(),
                    path,
                    FileOperationException.OperationType.DELETE,
                    serverName,
                    null
            );
        }

        log.info("File deleted successfully: {}", filePath);
    }

    public List<String> listFiles(String serverName, String directoryPath) {
        log.info("Listing files in: {} on server: {}", directoryPath, serverName);

        Path path = validatePath(directoryPath, FileOperationException.OperationType.LIST);

        // Check if directory exists
        CommandResult dirCheck = sshService.executeCommand(
                serverName,
                String.format("test -d '%s' && echo 'exists' || echo 'not_exists'", directoryPath),
                false
        );

        if (dirCheck.getOutput().trim().equals("not_exists")) {
            throw new ResourceNotFoundException("Directory", directoryPath);
        }

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("ls -la '%s'", directoryPath),
                false
        );

        if (!result.isSuccess()) {
            throw new FileOperationException(
                    "Failed to list files: " + result.getError(),
                    path,
                    FileOperationException.OperationType.LIST,
                    serverName,
                    null
            );
        }

        return Arrays.stream(result.getOutput().split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

    public void changePermissions(String serverName, String filePath, String permissions) {
        log.info("Changing permissions for: {} to: {} on server: {}",
                filePath, permissions, serverName);

        // Validate permissions format
        if (!permissions.matches("^[0-7]{3,4}$")) {
            throw new ValidationException(
                    "permissions",
                    permissions,
                    "Invalid permission format. Use octal notation (e.g., 755, 644)"
            );
        }

        Path path = validatePath(filePath, FileOperationException.OperationType.CHMOD);

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("chmod %s '%s'", permissions, filePath),
                true
        );

        if (!result.isSuccess()) {
            if (result.getError().contains("No such file")) {
                throw new ResourceNotFoundException("File/Directory", filePath);
            }
            throw new FileOperationException(
                    "Failed to change permissions: " + result.getError(),
                    path,
                    FileOperationException.OperationType.CHMOD,
                    serverName,
                    null
            );
        }

        log.info("Permissions changed successfully for: {}", filePath);
    }

    public void changeOwnership(String serverName, String filePath, String owner, String group) {
        log.info("Changing ownership for: {} to: {}:{} on server: {}",
                filePath, owner, group, serverName);

        // Validate owner and group
        if (owner == null || owner.trim().isEmpty()) {
            throw new ValidationException("owner", owner, "Owner cannot be empty");
        }
        if (group == null || group.trim().isEmpty()) {
            throw new ValidationException("group", group, "Group cannot be empty");
        }

        Path path = validatePath(filePath, FileOperationException.OperationType.CHOWN);

        CommandResult result = sshService.executeCommand(
                serverName,
                String.format("chown %s:%s '%s'", owner, group, filePath),
                true
        );

        if (!result.isSuccess()) {
            if (result.getError().contains("invalid user") ||
                    result.getError().contains("invalid group")) {
                throw new ValidationException(
                        "owner/group",
                        owner + ":" + group,
                        "Invalid user or group"
                );
            }
            if (result.getError().contains("No such file")) {
                throw new ResourceNotFoundException("File/Directory", filePath);
            }
            throw new FileOperationException(
                    "Failed to change ownership: " + result.getError(),
                    path,
                    FileOperationException.OperationType.CHOWN,
                    serverName,
                    null
            );
        }

        log.info("Ownership changed successfully for: {}", filePath);
    }

    private Path validatePath(String filePath, FileOperationException.OperationType operation) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("filePath", filePath, "File path cannot be empty");
        }

        try {
            Path path = Paths.get(filePath);

            if (!pathValidator.isValidPath(filePath)) {
                throw new FileOperationException(
                        "Path validation failed: Access to this path is restricted",
                        path,
                        operation
                );
            }

            return path;
        } catch (InvalidPathException e) {
            throw new ValidationException("filePath", filePath, "Invalid path format: " + e.getMessage());
        }
    }
}
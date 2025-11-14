package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.FileService;
import net.alishahidi.mcpconductor.security.PathValidator;
import net.alishahidi.mcpconductor.model.FileOperation;
import org.springframework.ai.mcp.server.annotation.McpTool;
import org.springframework.ai.mcp.server.annotation.McpToolParam;
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

    @McpTool(name = "file_read", description = "Read the contents of a file from a remote server. Perfect for viewing configuration files, logs, scripts, or any text-based files. Essential for debugging, configuration management, and file analysis.")
    public String readFile(
            @McpToolParam(description = "The full path to the file to read (e.g., '/etc/nginx/nginx.conf', '/var/log/app.log', '/home/user/script.sh'). Must be an absolute path to an existing file.") String filePath,
            @McpToolParam(description = "The target server identifier where the file is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Reading file: {} from server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        return fileService.readFile(serverName, filePath);
    }

    @McpTool(name = "file_write", description = "Write content to a file on a remote server, creating the file if it doesn't exist or overwriting existing content. Use for creating configuration files, scripts, or updating file contents completely.")
    public String writeFile(
            @McpToolParam(description = "The full path where the file should be written (e.g., '/etc/nginx/sites-available/mysite', '/home/user/backup.sh', '/tmp/config.json'). Parent directories must exist.") String filePath,
            @McpToolParam(description = "The text content to write to the file. Can be configuration text, script code, JSON data, or any text-based content. Use proper line breaks and formatting.") String content,
            @McpToolParam(description = "The target server identifier where the file should be written (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Writing to file: {} on server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.writeFile(serverName, filePath, content);
        return "File written successfully: " + filePath;
    }

    @McpTool(name = "file_append", description = "Append content to an existing file on a remote server without overwriting existing content. Perfect for adding entries to log files, configuration files, or accumulating data over time.")
    public String appendToFile(
            @McpToolParam(description = "The full path to the existing file to append to (e.g., '/var/log/custom.log', '/etc/hosts', '/home/user/.bashrc'). File must already exist.") String filePath,
            @McpToolParam(description = "The text content to append to the file. Will be added to the end of the existing file content. Include newlines as needed for proper formatting.") String content,
            @McpToolParam(description = "The target server identifier where the file is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Appending to file: {} on server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.appendToFile(serverName, filePath, content);
        return "Content appended to file: " + filePath;
    }

    @McpTool(name = "file_delete", description = "Delete a file from a remote server permanently. Use for cleanup, removing temporary files, or deleting obsolete configuration files. This operation cannot be undone.")
    public String deleteFile(
            @McpToolParam(description = "The full path to the file to delete (e.g., '/tmp/old_file.txt', '/var/cache/app/temp.dat', '/home/user/unused.log'). Must be an absolute path to an existing file.") String filePath,
            @McpToolParam(description = "The target server identifier where the file should be deleted (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Deleting file: {} from server: {}", filePath, serverName);

        if (!pathValidator.isValidPath(filePath)) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        fileService.deleteFile(serverName, filePath);
        return "File deleted: " + filePath;
    }

    @McpTool(name = "file_list", description = "List files and directories in a directory on a remote server. Essential for exploring file systems, finding files, checking directory contents, and understanding file structure.")
    public List<String> listFiles(
            @McpToolParam(description = "The full path to the directory to list (e.g., '/etc/', '/var/log/', '/home/user/', '/opt/apps/'). Must be an absolute path to an existing directory.") String directoryPath,
            @McpToolParam(description = "The target server identifier where the directory is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Listing files in: {} on server: {}", directoryPath, serverName);

        if (!pathValidator.isValidPath(directoryPath)) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        return fileService.listFiles(serverName, directoryPath);
    }

    @McpTool(name = "file_chmod", description = "Change file permissions on a remote server using chmod. Essential for security, making scripts executable, or controlling file access. Uses standard Unix permission notation.")
    public String changePermissions(
            @McpToolParam(description = "The full path to the file or directory to change permissions for (e.g., '/home/user/script.sh', '/etc/myapp/config', '/var/www/uploads/'). Must be an absolute path.") String filePath,
            @McpToolParam(description = "The permissions to set in octal notation (e.g., '755' for rwxr-xr-x, '644' for rw-r--r--, '600' for rw-------). Use 755 for executables, 644 for readable files, 600 for private files.") String permissions,
            @McpToolParam(description = "The target server identifier where the file is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Changing permissions for: {} to: {} on server: {}",
                filePath, permissions, serverName);

        fileService.changePermissions(serverName, filePath, permissions);
        return "Permissions changed for: " + filePath;
    }

    @McpTool(name = "file_chown", description = "Change file ownership on a remote server using chown. Important for security, process permissions, and proper file access control. Changes both user and group ownership.")
    public String changeOwnership(
            @McpToolParam(description = "The full path to the file or directory to change ownership for (e.g., '/var/www/html/index.html', '/etc/myapp/', '/home/user/data/'). Must be an absolute path.") String filePath,
            @McpToolParam(description = "The new owner username (e.g., 'www-data', 'root', 'nginx', 'app'). Must be a valid system user. Use the user that should own and control the file.") String owner,
            @McpToolParam(description = "The new group name (e.g., 'www-data', 'root', 'users', 'staff'). Must be a valid system group. Often the same as owner or a specific service group.") String group,
            @McpToolParam(description = "The target server identifier where the file is located (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Changing ownership for: {} to: {}:{} on server: {}",
                filePath, owner, group, serverName);

        fileService.changeOwnership(serverName, filePath, owner, group);
        return "Ownership changed for: " + filePath;
    }
}
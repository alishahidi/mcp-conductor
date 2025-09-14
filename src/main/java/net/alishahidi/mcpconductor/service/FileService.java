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
public class FileService {
    
    private final SSHService sshService;
    
    public String readFile(String serverName, String filePath) {
        CommandResult result = sshService.executeCommand(serverName, "cat " + filePath, false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to read file: " + result.getError());
        }
        return result.getOutput();
    }
    
    public void writeFile(String serverName, String filePath, String content) {
        String command = String.format("echo '%s' > %s", content.replace("'", "'\\''"), filePath);
        CommandResult result = sshService.executeCommand(serverName, command, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to write file: " + result.getError());
        }
    }
    
    public void appendToFile(String serverName, String filePath, String content) {
        String command = String.format("echo '%s' >> %s", content.replace("'", "'\\''"), filePath);
        CommandResult result = sshService.executeCommand(serverName, command, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to append to file: " + result.getError());
        }
    }
    
    public void deleteFile(String serverName, String filePath) {
        CommandResult result = sshService.executeCommand(serverName, "rm -f " + filePath, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to delete file: " + result.getError());
        }
    }
    
    public List<String> listFiles(String serverName, String directoryPath) {
        CommandResult result = sshService.executeCommand(serverName, "ls -la " + directoryPath, false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to list files: " + result.getError());
        }
        return Arrays.asList(result.getOutput().split("\n"));
    }
    
    public void changePermissions(String serverName, String filePath, String permissions) {
        CommandResult result = sshService.executeCommand(serverName, 
            String.format("chmod %s %s", permissions, filePath), true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to change permissions: " + result.getError());
        }
    }
    
    public void changeOwnership(String serverName, String filePath, String owner, String group) {
        CommandResult result = sshService.executeCommand(serverName, 
            String.format("chown %s:%s %s", owner, group, filePath), true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to change ownership: " + result.getError());
        }
    }
}
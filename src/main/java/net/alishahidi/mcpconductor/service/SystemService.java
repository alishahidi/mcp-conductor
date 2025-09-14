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
public class SystemService {
    
    private final SSHService sshService;
    
    public void startService(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl start " + serviceName, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to start service: " + result.getError());
        }
    }
    
    public void stopService(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl stop " + serviceName, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to stop service: " + result.getError());
        }
    }
    
    public void restartService(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl restart " + serviceName, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to restart service: " + result.getError());
        }
    }
    
    public String getServiceStatus(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl status " + serviceName, false);
        return result.getOutput();
    }
    
    public void enableService(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl enable " + serviceName, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to enable service: " + result.getError());
        }
    }
    
    public void disableService(String serverName, String serviceName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl disable " + serviceName, true);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to disable service: " + result.getError());
        }
    }
    
    public List<String> listServices(String serverName) {
        CommandResult result = sshService.executeCommand(serverName, 
            "systemctl list-units --type=service --all", false);
        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to list services: " + result.getError());
        }
        return Arrays.asList(result.getOutput().split("\n"));
    }
}
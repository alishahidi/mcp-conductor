package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.model.SystemInfo;
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
    
    public SystemInfo getSystemInfo(String serverName) {
        try {
            SystemInfo.SystemInfoBuilder builder = SystemInfo.builder();
            
            // Get hostname
            CommandResult hostnameResult = sshService.executeCommand(serverName, "hostname", false);
            if (hostnameResult.isSuccess()) {
                builder.hostname(hostnameResult.getOutput().trim());
            }
            
            // Get OS information
            CommandResult osResult = sshService.executeCommand(serverName, "cat /etc/os-release", false);
            if (osResult.isSuccess()) {
                String osInfo = parseOSInfo(osResult.getOutput());
                builder.operatingSystem(osInfo);
            }
            
            // Get kernel version
            CommandResult kernelResult = sshService.executeCommand(serverName, "uname -r", false);
            if (kernelResult.isSuccess()) {
                builder.kernelVersion(kernelResult.getOutput().trim());
            }
            
            // Get uptime
            CommandResult uptimeResult = sshService.executeCommand(serverName, "uptime -p", false);
            if (uptimeResult.isSuccess()) {
                builder.uptime(uptimeResult.getOutput().trim());
            }
            
            // Get CPU info
            CommandResult cpuResult = sshService.executeCommand(serverName, "nproc", false);
            if (cpuResult.isSuccess()) {
                try {
                    builder.cpuCores(Integer.parseInt(cpuResult.getOutput().trim()));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse CPU cores: {}", cpuResult.getOutput());
                }
            }
            
            // Get memory info
            CommandResult memResult = sshService.executeCommand(serverName, "free -h", false);
            if (memResult.isSuccess()) {
                String memInfo = parseMemoryInfo(memResult.getOutput());
                builder.memoryInfo(memInfo);
            }
            
            // Get disk usage
            CommandResult diskResult = sshService.executeCommand(serverName, "df -h /", false);
            if (diskResult.isSuccess()) {
                String diskInfo = parseDiskInfo(diskResult.getOutput());
                builder.diskUsage(diskInfo);
            }
            
            // Get load average
            CommandResult loadResult = sshService.executeCommand(serverName, "cat /proc/loadavg", false);
            if (loadResult.isSuccess()) {
                builder.loadAverage(loadResult.getOutput().trim());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Failed to get system info for server: {}", serverName, e);
            throw new RuntimeException("Failed to get system information: " + e.getMessage());
        }
    }
    
    private String parseOSInfo(String output) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("PRETTY_NAME=")) {
                return line.substring(12).replaceAll("\"", "");
            }
        }
        return "Unknown";
    }
    
    private String parseMemoryInfo(String output) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("Mem:")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    return String.format("Total: %s, Used: %s, Free: %s", 
                        parts[1], parts[2], parts[3]);
                }
            }
        }
        return "Unknown";
    }
    
    private String parseDiskInfo(String output) {
        String[] lines = output.split("\n");
        if (lines.length >= 2) {
            String[] parts = lines[1].trim().split("\\s+");
            if (parts.length >= 5) {
                return String.format("Size: %s, Used: %s, Available: %s, Use%%: %s", 
                    parts[1], parts[2], parts[3], parts[4]);
            }
        }
        return "Unknown";
    }
}
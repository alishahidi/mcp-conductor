package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.SSHService;
import net.alishahidi.mcpconductor.service.SystemService;
import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.model.SystemInfo;
import net.alishahidi.mcpconductor.util.ResponseFormatter;
import org.springframework.ai.mcp.server.annotation.McpTool;
import org.springframework.ai.mcp.server.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemMonitoringTool {
    
    private final SSHService sshService;
    private final SystemService systemService;
    private final ResponseFormatter responseFormatter;

    @McpTool(name = "get_system_info", description = "Get comprehensive system information including CPU, memory, disk usage, and system details from a remote server. Perfect for monitoring system health and resources.")
    public String getSystemInfo(
            @McpToolParam(description = "The target server name to get system information from (e.g., 'production', 'staging', 'localhost')") String serverName) {
        log.info("Getting system information for server: {}", serverName);
        
        try {
            SystemInfo systemInfo = systemService.getSystemInfo(serverName);
            return responseFormatter.formatSystemInfo(systemInfo.toMap());
        } catch (Exception e) {
            log.error("Failed to get system info for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get system information: " + e.getMessage());
        }
    }

    @McpTool(name = "get_process_list", description = "Get list of running processes on a remote server. Useful for monitoring what's running and identifying resource-intensive processes.")
    public String getProcessList(
            @McpToolParam(description = "The target server name to get process list from") String serverName,
            @McpToolParam(description = "Maximum number of processes to return (default: 20)") int limit) {
        log.info("Getting process list for server: {} (limit: {})", serverName, limit);
        
        try {
            String command = limit > 0 ? 
                String.format("ps aux --sort=-%scpu | head -%d", "%", limit + 1) : 
                "ps aux --sort=-%cpu";
                
            CommandResult result = sshService.executeCommand(serverName, command, false);
            
            if (result.isSuccess()) {
                return responseFormatter.formatSuccess("Process list retrieved successfully", 
                    parseProcessList(result.getOutput()));
            } else {
                return responseFormatter.formatError("Failed to get process list: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get process list for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get process list: " + e.getMessage());
        }
    }

    @McpTool(name = "get_service_status", description = "Check the status of a specific system service (systemd) on a remote server. Useful for monitoring critical services.")
    public String getServiceStatus(
            @McpToolParam(description = "The target server name to check service on") String serverName,
            @McpToolParam(description = "The name of the service to check (e.g., 'nginx', 'apache2', 'mysql', 'docker')") String serviceName) {
        log.info("Checking service status for {} on server: {}", serviceName, serverName);
        
        try {
            String command = String.format("systemctl status %s", serviceName);
            CommandResult result = sshService.executeCommand(serverName, command, false);
            
            Map<String, Object> serviceDetails = parseServiceStatus(result.getOutput());
            return responseFormatter.formatServiceStatus(serviceName, 
                serviceDetails.get("status").toString(), serviceDetails);
                
        } catch (Exception e) {
            log.error("Failed to get service status for {} on server: {}", serviceName, serverName, e);
            return responseFormatter.formatError("Failed to get service status: " + e.getMessage());
        }
    }

    @McpTool(name = "get_disk_usage", description = "Get disk space usage information for all mounted filesystems on a remote server. Essential for monitoring storage capacity.")
    public String getDiskUsage(
            @McpToolParam(description = "The target server name to check disk usage on") String serverName) {
        log.info("Getting disk usage for server: {}", serverName);
        
        try {
            CommandResult result = sshService.executeCommand(serverName, "df -h", false);
            
            if (result.isSuccess()) {
                List<Map<String, Object>> diskInfo = parseDiskUsage(result.getOutput());
                return responseFormatter.formatList(diskInfo, "Disk usage information");
            } else {
                return responseFormatter.formatError("Failed to get disk usage: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get disk usage for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get disk usage: " + e.getMessage());
        }
    }

    @McpTool(name = "get_memory_usage", description = "Get detailed memory usage information including RAM and swap usage on a remote server.")
    public String getMemoryUsage(
            @McpToolParam(description = "The target server name to check memory usage on") String serverName) {
        log.info("Getting memory usage for server: {}", serverName);
        
        try {
            CommandResult result = sshService.executeCommand(serverName, "free -h", false);
            
            if (result.isSuccess()) {
                Map<String, Object> memoryInfo = parseMemoryUsage(result.getOutput());
                return responseFormatter.formatMap(memoryInfo, "Memory usage information");
            } else {
                return responseFormatter.formatError("Failed to get memory usage: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get memory usage for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get memory usage: " + e.getMessage());
        }
    }

    @McpTool(name = "get_network_info", description = "Get network interface information and statistics from a remote server. Useful for monitoring network connectivity and usage.")
    public String getNetworkInfo(
            @McpToolParam(description = "The target server name to get network information from") String serverName) {
        log.info("Getting network info for server: {}", serverName);
        
        try {
            CommandResult result = sshService.executeCommand(serverName, "ip addr show", false);
            
            if (result.isSuccess()) {
                List<Map<String, Object>> networkInterfaces = parseNetworkInfo(result.getOutput());
                return responseFormatter.formatList(networkInterfaces, "Network interface information");
            } else {
                return responseFormatter.formatError("Failed to get network info: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get network info for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get network info: " + e.getMessage());
        }
    }

    @McpTool(name = "get_load_average", description = "Get system load average (1min, 5min, 15min) from a remote server. Critical for understanding system performance under load.")
    public String getLoadAverage(
            @McpToolParam(description = "The target server name to get load average from") String serverName) {
        log.info("Getting load average for server: {}", serverName);
        
        try {
            CommandResult result = sshService.executeCommand(serverName, "uptime", false);
            
            if (result.isSuccess()) {
                Map<String, Object> loadInfo = parseLoadAverage(result.getOutput());
                return responseFormatter.formatMap(loadInfo, "System load average");
            } else {
                return responseFormatter.formatError("Failed to get load average: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get load average for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get load average: " + e.getMessage());
        }
    }

    @McpTool(name = "get_top_processes", description = "Get top CPU and memory consuming processes from a remote server. Perfect for identifying resource bottlenecks.")
    public String getTopProcesses(
            @McpToolParam(description = "The target server name to get top processes from") String serverName,
            @McpToolParam(description = "Sort by 'cpu' or 'memory' (default: cpu)") String sortBy,
            @McpToolParam(description = "Number of top processes to return (default: 10)") int count) {
        log.info("Getting top processes for server: {} (sorted by: {}, count: {})", serverName, sortBy, count);
        
        try {
            String sortColumn = "cpu".equalsIgnoreCase(sortBy) ? "%cpu" : "%mem";
            String command = String.format("ps aux --sort=-%s | head -%d", sortColumn, count + 1);
            
            CommandResult result = sshService.executeCommand(serverName, command, false);
            
            if (result.isSuccess()) {
                List<Map<String, Object>> processes = parseProcessList(result.getOutput());
                return responseFormatter.formatList(processes, 
                    String.format("Top %d processes by %s", count, sortBy));
            } else {
                return responseFormatter.formatError("Failed to get top processes: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to get top processes for server: {}", serverName, e);
            return responseFormatter.formatError("Failed to get top processes: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parseProcessList(String output) {
        List<Map<String, Object>> processes = new ArrayList<>();
        String[] lines = output.split("\n");
        
        if (lines.length < 2) return processes;
        
        for (int i = 1; i < lines.length; i++) { // Skip header
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split("\\s+", 11);
            if (parts.length >= 11) {
                Map<String, Object> process = new HashMap<>();
                process.put("user", parts[0]);
                process.put("pid", parts[1]);
                process.put("cpu", parts[2]);
                process.put("memory", parts[3]);
                process.put("vsz", parts[4]);
                process.put("rss", parts[5]);
                process.put("tty", parts[6]);
                process.put("stat", parts[7]);
                process.put("start", parts[8]);
                process.put("time", parts[9]);
                process.put("command", parts[10]);
                processes.add(process);
            }
        }
        
        return processes;
    }

    private Map<String, Object> parseServiceStatus(String output) {
        Map<String, Object> serviceInfo = new HashMap<>();
        String[] lines = output.split("\n");
        
        serviceInfo.put("status", "unknown");
        serviceInfo.put("active", false);
        serviceInfo.put("enabled", false);
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("Active:")) {
                if (line.contains("active (running)")) {
                    serviceInfo.put("status", "running");
                    serviceInfo.put("active", true);
                } else if (line.contains("inactive")) {
                    serviceInfo.put("status", "inactive");
                } else if (line.contains("failed")) {
                    serviceInfo.put("status", "failed");
                }
            } else if (line.contains("Loaded:")) {
                serviceInfo.put("enabled", line.contains("enabled"));
            }
        }
        
        serviceInfo.put("output", output);
        return serviceInfo;
    }

    private List<Map<String, Object>> parseDiskUsage(String output) {
        List<Map<String, Object>> disks = new ArrayList<>();
        String[] lines = output.split("\n");
        
        if (lines.length < 2) return disks;
        
        for (int i = 1; i < lines.length; i++) { // Skip header
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split("\\s+");
            if (parts.length >= 6) {
                Map<String, Object> disk = new HashMap<>();
                disk.put("filesystem", parts[0]);
                disk.put("size", parts[1]);
                disk.put("used", parts[2]);
                disk.put("available", parts[3]);
                disk.put("usePercent", parts[4]);
                disk.put("mountedOn", parts[5]);
                disks.add(disk);
            }
        }
        
        return disks;
    }

    private Map<String, Object> parseMemoryUsage(String output) {
        Map<String, Object> memory = new HashMap<>();
        String[] lines = output.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Mem:")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 7) {
                    memory.put("total", parts[1]);
                    memory.put("used", parts[2]);
                    memory.put("free", parts[3]);
                    memory.put("shared", parts[4]);
                    memory.put("buffCache", parts[5]);
                    memory.put("available", parts[6]);
                }
            } else if (line.startsWith("Swap:")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    memory.put("swapTotal", parts[1]);
                    memory.put("swapUsed", parts[2]);
                    memory.put("swapFree", parts[3]);
                }
            }
        }
        
        return memory;
    }

    private List<Map<String, Object>> parseNetworkInfo(String output) {
        List<Map<String, Object>> interfaces = new ArrayList<>();
        String[] lines = output.split("\n");
        
        Map<String, Object> currentInterface = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^\\d+:.*")) {
                if (currentInterface != null) {
                    interfaces.add(currentInterface);
                }
                currentInterface = new HashMap<>();
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    currentInterface.put("name", parts[1].trim().split("\\s+")[0]);
                    currentInterface.put("flags", line.contains("<") ? 
                        line.substring(line.indexOf("<"), line.indexOf(">") + 1) : "");
                }
            } else if (currentInterface != null && line.startsWith("inet ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    currentInterface.put("ipv4", parts[1]);
                }
            } else if (currentInterface != null && line.startsWith("inet6 ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    currentInterface.put("ipv6", parts[1]);
                }
            }
        }
        
        if (currentInterface != null) {
            interfaces.add(currentInterface);
        }
        
        return interfaces;
    }

    private Map<String, Object> parseLoadAverage(String output) {
        Map<String, Object> loadInfo = new HashMap<>();
        
        if (output.contains("load average:")) {
            String loadPart = output.substring(output.indexOf("load average:") + 13).trim();
            String[] loads = loadPart.split(",");
            
            if (loads.length >= 3) {
                loadInfo.put("load1min", loads[0].trim());
                loadInfo.put("load5min", loads[1].trim());
                loadInfo.put("load15min", loads[2].trim());
            }
        }
        
        if (output.contains("up ")) {
            loadInfo.put("uptime", output.substring(output.indexOf("up "), 
                output.indexOf(",", output.indexOf("up "))).trim());
        }
        
        return loadInfo;
    }
}
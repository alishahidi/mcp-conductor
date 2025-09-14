package net.alishahidi.mcpconductor.tools;

import com.devops.mcp.service.SystemService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementTool {

    private final SystemService systemService;

    @Tool(description = "Start a system service")
    public String startService(String serviceName, String serverName) {
        log.info("Starting service: {} on server: {}", serviceName, serverName);
        systemService.startService(serverName, serviceName);
        return "Service started: " + serviceName;
    }

    @Tool(description = "Stop a system service")
    public String stopService(String serviceName, String serverName) {
        log.info("Stopping service: {} on server: {}", serviceName, serverName);
        systemService.stopService(serverName, serviceName);
        return "Service stopped: " + serviceName;
    }

    @Tool(description = "Restart a system service")
    public String restartService(String serviceName, String serverName) {
        log.info("Restarting service: {} on server: {}", serviceName, serverName);
        systemService.restartService(serverName, serviceName);
        return "Service restarted: " + serviceName;
    }

    @Tool(description = "Get service status")
    public String getServiceStatus(String serviceName, String serverName) {
        log.info("Getting status for service: {} on server: {}", serviceName, serverName);
        return systemService.getServiceStatus(serverName, serviceName);
    }

    @Tool(description = "Enable service to start on boot")
    public String enableService(String serviceName, String serverName) {
        log.info("Enabling service: {} on server: {}", serviceName, serverName);
        systemService.enableService(serverName, serviceName);
        return "Service enabled: " + serviceName;
    }

    @Tool(description = "Disable service from starting on boot")
    public String disableService(String serviceName, String serverName) {
        log.info("Disabling service: {} on server: {}", serviceName, serverName);
        systemService.disableService(serverName, serviceName);
        return "Service disabled: " + serviceName;
    }

    @Tool(description = "List all system services")
    public List<String> listServices(String serverName) {
        log.info("Listing services on server: {}", serverName);
        return systemService.listServices(serverName);
    }
}
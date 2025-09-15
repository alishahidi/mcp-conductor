package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.SystemService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementTool {

    private final SystemService systemService;

    @Tool(name = "service_start", description = "Start a system service on a remote server using systemctl. Essential for bringing services online, starting applications after maintenance, or recovering from service failures. Works with systemd-managed services.")
    public String startService(
            @ToolParam(description = "The name of the systemd service to start (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service should be started (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Starting service: {} on server: {}", serviceName, serverName);
        systemService.startService(serverName, serviceName);
        return "Service started: " + serviceName;
    }

    @Tool(name = "service_stop", description = "Stop a system service on a remote server using systemctl. Use for maintenance, updates, troubleshooting, or when services need to be temporarily disabled. Sends graceful shutdown signals to the service.")
    public String stopService(
            @ToolParam(description = "The name of the systemd service to stop (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid running service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service should be stopped (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Stopping service: {} on server: {}", serviceName, serverName);
        systemService.stopService(serverName, serviceName);
        return "Service stopped: " + serviceName;
    }

    @Tool(name = "service_restart", description = "Restart a system service on a remote server using systemctl. Perfect for applying configuration changes, recovering from issues, or refreshing service state. Combines stop and start operations.")
    public String restartService(
            @ToolParam(description = "The name of the systemd service to restart (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service should be restarted (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Restarting service: {} on server: {}", serviceName, serverName);
        systemService.restartService(serverName, serviceName);
        return "Service restarted: " + serviceName;
    }

    @Tool(name = "service_status", description = "Get the status of a system service on a remote server using systemctl. Shows if service is running, failed, stopped, or in other states. Essential for monitoring and troubleshooting service health.")
    public String getServiceStatus(
            @ToolParam(description = "The name of the systemd service to check status for (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service status should be checked (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Getting status for service: {} on server: {}", serviceName, serverName);
        return systemService.getServiceStatus(serverName, serviceName);
    }

    @Tool(name = "service_enable", description = "Enable a system service to start automatically on boot using systemctl. Essential for ensuring critical services start after server reboots. Sets up service for automatic startup without starting it immediately.")
    public String enableService(
            @ToolParam(description = "The name of the systemd service to enable for auto-start (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service should be enabled (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Enabling service: {} on server: {}", serviceName, serverName);
        systemService.enableService(serverName, serviceName);
        return "Service enabled: " + serviceName;
    }

    @Tool(name = "service_disable", description = "Disable a system service from starting automatically on boot using systemctl. Use when services should not auto-start, for maintenance mode, or when decommissioning services. Does not stop currently running service.")
    public String disableService(
            @ToolParam(description = "The name of the systemd service to disable from auto-start (e.g., 'nginx', 'docker', 'postgresql', 'mysql', 'apache2'). Must be a valid service unit name without .service extension.") String serviceName,
            @ToolParam(description = "The target server identifier where the service should be disabled (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Disabling service: {} on server: {}", serviceName, serverName);
        systemService.disableService(serverName, serviceName);
        return "Service disabled: " + serviceName;
    }

    @Tool(name = "service_list", description = "List all available system services on a remote server using systemctl. Perfect for discovering installed services, checking what's available for management, and understanding the service landscape on the server.")
    public List<String> listServices(
            @ToolParam(description = "The target server identifier where services should be listed (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName) {
        log.info("Listing services on server: {}", serverName);
        return systemService.listServices(serverName);
    }
}
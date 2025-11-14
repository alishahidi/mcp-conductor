package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.DockerService;
import net.alishahidi.mcpconductor.model.DockerContainer;
import org.springframework.ai.mcp.server.annotation.McpTool;
import org.springframework.ai.mcp.server.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DockerManagementTool {

    private final DockerService dockerService;

    @McpTool(name = "docker_list_containers", description = "List Docker containers on the system. Use this to see running containers or all containers including stopped ones. Perfect for monitoring container status, getting container IDs, and managing containerized applications.")
    public List<DockerContainer> listContainers(
            @McpToolParam(description = "Whether to show all containers including stopped ones (true) or only running containers (false). Use true to see the complete container inventory, false for active containers only.") boolean showAll) {
        log.info("Listing Docker containers, showAll: {}", showAll);
        return dockerService.listContainers(showAll);
    }

    @McpTool(name = "docker_pull_image", description = "Pull a Docker image from a registry (Docker Hub by default). Use this to download images before running containers. Essential for getting the latest versions or specific versions of applications.")
    public String pullImage(
            @McpToolParam(description = "The Docker image name to pull (e.g., 'nginx', 'ubuntu', 'node', 'postgres'). Can include registry URL like 'registry.example.com/myapp'.") String imageName,
            @McpToolParam(description = "The image tag/version to pull (e.g., 'latest', '14-alpine', '1.21', 'stable'). Use 'latest' for the newest version or specific version tags for reproducible deployments.") String tag) {
        log.info("Pulling Docker image: {}:{}", imageName, tag);
        dockerService.pullImage(imageName, tag);
        return "Image pulled successfully: " + imageName + ":" + tag;
    }

    @McpTool(name = "docker_run_container", description = "Run a new Docker container from an image. This creates and starts a container with specified configuration including environment variables, port mappings, and volume mounts. Perfect for deploying applications, databases, and services.")
    public String runContainer(
            @McpToolParam(description = "The Docker image name to run (e.g., 'nginx:latest', 'postgres:13', 'node:16-alpine'). Must be available locally or will be pulled automatically.") String imageName,
            @McpToolParam(description = "A unique name for the container (e.g., 'my-web-server', 'prod-database', 'api-service'). Used for container management and networking.") String containerName,
            @McpToolParam(description = "Environment variables as key-value pairs (e.g., {'NODE_ENV': 'production', 'DATABASE_URL': 'postgres://...'} ). Use for application configuration and secrets.") Map<String, String> environment,
            @McpToolParam(description = "Port mappings from host to container (e.g., {'8080': '80', '5432': '5432'}). Format: hostPort:containerPort. Essential for accessing services from outside the container.") Map<String, String> ports,
            @McpToolParam(description = "Volume mounts for persistent data (e.g., ['/host/path:/container/path', '/var/lib/docker/volumes/mydata:/data']). Use for databases, logs, and persistent application data.") List<String> volumes) {
        log.info("Running container: {} from image: {}", containerName, imageName);

        String containerId = dockerService.runContainer(
                imageName, containerName, environment, ports, volumes
        );

        return "Container started with ID: " + containerId;
    }

    @McpTool(name = "docker_stop_container", description = "Stop a running Docker container gracefully. Sends SIGTERM signal to allow clean shutdown. Use this for maintenance, updates, or when services are no longer needed.")
    public String stopContainer(
            @McpToolParam(description = "The container ID or name to stop (e.g., 'abc123def456', 'my-web-server', 'prod-database'). Can be full ID, short ID, or the container name.") String containerId) {
        log.info("Stopping container: {}", containerId);
        dockerService.stopContainer(containerId);
        return "Container stopped: " + containerId;
    }

    @McpTool(name = "docker_remove_container", description = "Remove a Docker container permanently. This deletes the container and its filesystem (but not volumes). Use after stopping containers to free up disk space and clean up unused containers.")
    public String removeContainer(
            @McpToolParam(description = "The container ID or name to remove (e.g., 'abc123def456', 'my-web-server', 'old-container'). Can be full ID, short ID, or container name.") String containerId,
            @McpToolParam(description = "Whether to force removal of running containers (true/false). Use true to remove running containers (sends SIGKILL), false to only remove stopped containers safely.") boolean force) {
        log.info("Removing container: {}, force: {}", containerId, force);
        dockerService.removeContainer(containerId, force);
        return "Container removed: " + containerId;
    }

    @McpTool(name = "docker_get_logs", description = "Get logs from a Docker container for debugging and monitoring. Shows application output, error messages, and system logs. Essential for troubleshooting container issues and monitoring application behavior.")
    public String getContainerLogs(
            @McpToolParam(description = "The container ID or name to get logs from (e.g., 'abc123def456', 'my-web-server', 'api-service'). Can be full ID, short ID, or container name.") String containerId,
            @McpToolParam(description = "Number of recent log lines to retrieve (e.g., 100, 500, 1000). Use smaller numbers for quick checks, larger numbers for detailed analysis. Use 0 for all logs.") int tailLines) {
        log.info("Getting logs for container: {}, tail: {}", containerId, tailLines);
        return dockerService.getContainerLogs(containerId, tailLines);
    }

    @McpTool(name = "docker_exec_command", description = "Execute a command inside a running Docker container. Perfect for debugging, maintenance tasks, running scripts, checking file contents, or interactive troubleshooting within the container environment.")
    public String execInContainer(
            @McpToolParam(description = "The container ID or name to execute command in (e.g., 'abc123def456', 'my-web-server', 'database-container'). Must be a running container.") String containerId,
            @McpToolParam(description = "The command to execute inside the container (e.g., 'ls -la', 'ps aux', 'cat /etc/nginx/nginx.conf', 'npm install'). Use Linux commands appropriate for the container's operating system.") String command) {
        log.info("Executing command in container: {}", containerId);
        return dockerService.execInContainer(containerId, command);
    }
}
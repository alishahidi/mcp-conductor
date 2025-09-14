package net.alishahidi.mcpconductor.tools;

import com.devops.mcp.service.DockerService;
import com.devops.mcp.model.DockerContainer;
import org.springframework.ai.tool.annotation.Tool;
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

    @Tool(description = "List all Docker containers on the server")
    public List<DockerContainer> listContainers(boolean showAll) {
        log.info("Listing Docker containers, showAll: {}", showAll);
        return dockerService.listContainers(showAll);
    }

    @Tool(description = "Pull a Docker image from registry")
    public String pullImage(String imageName, String tag) {
        log.info("Pulling Docker image: {}:{}", imageName, tag);
        dockerService.pullImage(imageName, tag);
        return "Image pulled successfully: " + imageName + ":" + tag;
    }

    @Tool(description = "Run a new Docker container")
    public String runContainer(String imageName,
                               String containerName,
                               Map<String, String> environment,
                               Map<String, String> ports,
                               List<String> volumes) {
        log.info("Running container: {} from image: {}", containerName, imageName);

        String containerId = dockerService.runContainer(
                imageName, containerName, environment, ports, volumes
        );

        return "Container started with ID: " + containerId;
    }

    @Tool(description = "Stop a running Docker container")
    public String stopContainer(String containerId) {
        log.info("Stopping container: {}", containerId);
        dockerService.stopContainer(containerId);
        return "Container stopped: " + containerId;
    }

    @Tool(description = "Remove a Docker container")
    public String removeContainer(String containerId, boolean force) {
        log.info("Removing container: {}, force: {}", containerId, force);
        dockerService.removeContainer(containerId, force);
        return "Container removed: " + containerId;
    }

    @Tool(description = "Get Docker container logs")
    public String getContainerLogs(String containerId, int tailLines) {
        log.info("Getting logs for container: {}, tail: {}", containerId, tailLines);
        return dockerService.getContainerLogs(containerId, tailLines);
    }

    @Tool(description = "Execute command inside Docker container")
    public String execInContainer(String containerId, String command) {
        log.info("Executing command in container: {}", containerId);
        return dockerService.execInContainer(containerId, command);
    }
}
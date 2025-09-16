package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.exception.*;
import net.alishahidi.mcpconductor.model.DockerContainer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;

    @Cacheable(value = "docker-containers", unless = "#result.isEmpty()")
    public List<DockerContainer> listContainers(boolean showAll) {
        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(showAll)
                    .exec();

            return containers.stream()
                    .map(this::mapToDockerContainer)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to list containers", e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to list containers: " + e.getMessage(),
                    "LIST_CONTAINERS"
            );
        }
    }

    public void pullImage(String imageName, String tag) {
        String fullImageName = imageName + ":" + (tag != null ? tag : "latest");

        try {
            log.info("Pulling Docker image: {}", fullImageName);

            dockerClient.pullImageCmd(fullImageName)
                    .exec(new PullImageResultCallback() {
                        @Override
                        public void onError(Throwable throwable) {
                            log.error("Error pulling image: {}", fullImageName, throwable);
                            throw new net.alishahidi.mcpconductor.exception.DockerException(
                                    "Failed to pull image: " + throwable.getMessage(),
                                    fullImageName,
                                    "PULL",
                                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.PULL_FAILED
                            );
                        }
                    })
                    .awaitCompletion(5, TimeUnit.MINUTES);

            log.info("Image pulled successfully: {}", fullImageName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Image pull was interrupted",
                    fullImageName,
                    "PULL",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.PULL_FAILED
            );
        } catch (Exception e) {
            if (e instanceof net.alishahidi.mcpconductor.exception.DockerException) {
                throw e;
            }
            log.error("Failed to pull image: {}", imageName, e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to pull image: " + e.getMessage(),
                    e,
                    "PULL"
            );
        }
    }

    public String runContainer(String imageName,
                               String containerName,
                               Map<String, String> environment,
                               Map<String, String> ports,
                               List<String> volumes) {

        // Validate container name doesn't already exist
        if (containerName != null) {
            try {
                dockerClient.inspectContainerCmd(containerName).exec();
                throw new ConflictException("Container with name '" + containerName + "' already exists");
            } catch (NotFoundException e) {
                // Container doesn't exist, which is what we want
            }
        }

        try {
            CreateContainerCmd createCmd = dockerClient.createContainerCmd(imageName);

            if (containerName != null) {
                createCmd.withName(containerName);
            }

            if (environment != null && !environment.isEmpty()) {
                List<String> env = environment.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.toList());
                createCmd.withEnv(env);
            }

            if (ports != null && !ports.isEmpty()) {
                ExposedPort[] exposedPorts = ports.keySet().stream()
                        .map(ExposedPort::parse)
                        .toArray(ExposedPort[]::new);

                Ports portBindings = new Ports();
                ports.forEach((containerPort, hostPort) -> {
                    ExposedPort exposedPort = ExposedPort.parse(containerPort);
                    Ports.Binding binding = Ports.Binding.bindPort(Integer.parseInt(hostPort));
                    portBindings.bind(exposedPort, binding);
                });

                createCmd.withExposedPorts(exposedPorts)
                        .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings));
            }

            if (volumes != null && !volumes.isEmpty()) {
                List<Volume> volumeList = volumes.stream()
                        .map(Volume::new)
                        .collect(Collectors.toList());
                createCmd.withVolumes(volumeList);
            }

            CreateContainerResponse container = createCmd.exec();

            try {
                dockerClient.startContainerCmd(container.getId()).exec();
            } catch (Exception e) {
                // Clean up the created container if start fails
                try {
                    dockerClient.removeContainerCmd(container.getId()).exec();
                } catch (Exception cleanupEx) {
                    log.warn("Failed to clean up container after start failure", cleanupEx);
                }
                throw new net.alishahidi.mcpconductor.exception.DockerException(
                        "Failed to start container: " + e.getMessage(),
                        container.getId(),
                        "START",
                        net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.START_FAILED
                );
            }

            log.info("Container started: {}", container.getId());
            return container.getId();

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Docker image", imageName);
        } catch (ConflictException e) {
            throw new ValidationException("containerName", containerName,
                    "Container name already exists");
        } catch (Exception e) {
            if (e instanceof net.alishahidi.mcpconductor.exception.DockerException ||
                    e instanceof ResourceNotFoundException ||
                    e instanceof ValidationException) {
                throw e;
            }
            log.error("Failed to run container", e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to run container: " + e.getMessage(),
                    e,
                    "RUN"
            );
        }
    }

    public void stopContainer(String containerId) {
        try {
            // First check if container exists
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();

            if (!containerInfo.getState().getRunning()) {
                log.info("Container {} is not running", containerId);
                return;
            }

            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(30)
                    .exec();

            log.info("Container stopped: {}", containerId);

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Container", containerId);
        } catch (Exception e) {
            log.error("Failed to stop container: {}", containerId, e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to stop container: " + e.getMessage(),
                    containerId,
                    "STOP",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.STOP_FAILED
            );
        }
    }

    public void removeContainer(String containerId, boolean force) {
        try {
            dockerClient.removeContainerCmd(containerId)
                    .withForce(force)
                    .exec();

            log.info("Container removed: {}", containerId);

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Container", containerId);
        } catch (ConflictException e) {
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Container is still running. Use force=true to remove running container",
                    containerId,
                    "REMOVE",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.STOP_FAILED
            );
        } catch (Exception e) {
            log.error("Failed to remove container: {}", containerId, e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to remove container: " + e.getMessage(),
                    containerId,
                    "REMOVE",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.NETWORK_ERROR
            );
        }
    }

    public String getContainerLogs(String containerId, int tailLines) {
        try {
            // Validate container exists
            dockerClient.inspectContainerCmd(containerId).exec();

            StringBuilder logs = new StringBuilder();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(tailLines)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            logs.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();

            return logs.toString();

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Container", containerId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Log retrieval was interrupted",
                    containerId,
                    "LOGS",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.NETWORK_ERROR
            );
        } catch (Exception e) {
            log.error("Failed to get container logs: {}", containerId, e);
            throw new net.alishahidi.mcpconductor.exception.DockerException(
                    "Failed to get container logs: " + e.getMessage(),
                    containerId,
                    "LOGS",
                    net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.NETWORK_ERROR
            );
        }
    }

    public String execInContainer(String containerId, String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new ValidationException("command", command, "Command cannot be empty");
        }

        try {
            // Validate container exists and is running
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();

            if (!containerInfo.getState().getRunning()) {
                throw new net.alishahidi.mcpconductor.exception.DockerException(
                        "Container is not running",
                        containerId,
                        "EXEC",
                        net.alishahidi.mcpconductor.exception.DockerException.ErrorCode.STOP_FAILED
                );
            }

            ExecCreateCmdResponse execCreateResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            dockerClient.execStartCmd(execCreateResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, outputStream))
                    .awaitCompletion();

            return outputStream.toString();

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Container", containerId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException(
                    "Command execution was interrupted",
                    command,
                    containerId,
                    -1
            );
        } catch (Exception e) {
            if (e instanceof net.alishahidi.mcpconductor.exception.DockerException ||
                    e instanceof ResourceNotFoundException ||
                    e instanceof ValidationException) {
                throw e;
            }
            log.error("Failed to execute command in container: {}", containerId, e);
            throw new CommandExecutionException(
                    "Failed to execute command in container: " + e.getMessage(),
                    command,
                    containerId,
                    -1
            );
        }
    }

    private DockerContainer mapToDockerContainer(Container container) {
        return DockerContainer.builder()
                .id(container.getId())
                .names(Arrays.asList(container.getNames()))
                .image(container.getImage())
                .imageId(container.getImageId())
                .command(container.getCommand())
                .created(container.getCreated())
                .status(container.getStatus())
                .state(container.getState())
                .ports(mapPorts(container.getPorts()))
                .labels(container.getLabels())
                .networkMode(container.getHostConfig() != null ?
                        container.getHostConfig().getNetworkMode() : null)
                .mounts(container.getMounts() != null ?
                        container.getMounts().stream()
                                .map(mount -> mount.getSource() != null ? mount.getSource() : "")
                                .collect(Collectors.toList()) : null)
                .build();
    }

    private List<String> mapPorts(ContainerPort[] ports) {
        if (ports == null) return Collections.emptyList();

        return Arrays.stream(ports)
                .map(p -> String.format("%s:%s->%s/%s",
                        p.getIp() != null ? p.getIp() : "0.0.0.0",
                        p.getPublicPort() != null ? p.getPublicPort() : "",
                        p.getPrivatePort(),
                        p.getType()))
                .collect(Collectors.toList());
    }
}
package net.alishahidi.mcpconductor.service;

import com.devops.mcp.model.DockerContainer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            throw new RuntimeException("Failed to list containers: " + e.getMessage());
        }
    }

    public void pullImage(String imageName, String tag) {
        try {
            String fullImageName = imageName + ":" + (tag != null ? tag : "latest");

            dockerClient.pullImageCmd(fullImageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(5, TimeUnit.MINUTES);

            log.info("Image pulled successfully: {}", fullImageName);
        } catch (Exception e) {
            log.error("Failed to pull image: {}", imageName, e);
            throw new RuntimeException("Failed to pull image: " + e.getMessage());
        }
    }

    public String runContainer(String imageName,
                               String containerName,
                               Map<String, String> environment,
                               Map<String, String> ports,
                               List<String> volumes) {
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
            dockerClient.startContainerCmd(container.getId()).exec();

            log.info("Container started: {}", container.getId());
            return container.getId();

        } catch (Exception e) {
            log.error("Failed to run container", e);
            throw new RuntimeException("Failed to run container: " + e.getMessage());
        }
    }

    public void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(30)
                    .exec();
            log.info("Container stopped: {}", containerId);
        } catch (Exception e) {
            log.error("Failed to stop container: {}", containerId, e);
            throw new RuntimeException("Failed to stop container: " + e.getMessage());
        }
    }

    public void removeContainer(String containerId, boolean force) {
        try {
            dockerClient.removeContainerCmd(containerId)
                    .withForce(force)
                    .exec();
            log.info("Container removed: {}", containerId);
        } catch (Exception e) {
            log.error("Failed to remove container: {}", containerId, e);
            throw new RuntimeException("Failed to remove container: " + e.getMessage());
        }
    }

    public String getContainerLogs(String containerId, int tailLines) {
        try {
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
        } catch (Exception e) {
            log.error("Failed to get container logs: {}", containerId, e);
            throw new RuntimeException("Failed to get container logs: " + e.getMessage());
        }
    }

    public String execInContainer(String containerId, String command) {
        try {
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
        } catch (Exception e) {
            log.error("Failed to execute command in container: {}", containerId, e);
            throw new RuntimeException("Failed to execute command in container: " + e.getMessage());
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
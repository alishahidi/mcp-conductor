package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.config.SSHProperties;
import net.alishahidi.mcpconductor.exception.*;
import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.util.SSHConnectionPool;
import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SSHService {

    private final SSHConnectionPool connectionPool;
    private final SSHProperties sshProperties;

    @Retryable(
            value = {SSHConnectionException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CommandResult executeCommand(String serverName, String command, boolean useSudo) {
        Session session = null;
        ChannelExec channel = null;

        try {
            // Validate server configuration
            if (!sshProperties.getServers().containsKey(serverName) &&
                    !serverName.equals("localhost")) {
                throw new ConfigurationException(
                        "Server configuration not found",
                        serverName,
                        "application.yml"
                );
            }

            // Get connection with proper exception handling
            try {
                session = connectionPool.getConnection(serverName);
            } catch (JSchException e) {
                throw new SSHConnectionException(
                        "Failed to establish SSH connection",
                        sshProperties.getServers().get(serverName).getHost(),
                        sshProperties.getServers().get(serverName).getPort(),
                        sshProperties.getServers().get(serverName).getUsername(),
                        e
                );
            }

            channel = (ChannelExec) session.openChannel("exec");

            // Prepare command with sudo if needed
            String finalCommand = prepareSudoCommand(serverName, command, useSudo);
            channel.setCommand(finalCommand);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            channel.setOutputStream(outputStream);
            channel.setErrStream(errorStream);

            try {
                channel.connect(sshProperties.getCommandTimeout());
            } catch (JSchException e) {
                throw new CommandExecutionException(
                        "Failed to execute command: " + e.getMessage(),
                        command,
                        serverName,
                        -1
                );
            }

            // Wait for command completion with timeout
            long startTime = System.currentTimeMillis();
            while (!channel.isClosed()) {
                if (System.currentTimeMillis() - startTime > sshProperties.getCommandTimeout()) {
                    throw new CommandExecutionException(
                            "Command execution timeout exceeded",
                            command,
                            serverName,
                            -1
                    );
                }
                Thread.sleep(100);
            }

            int exitCode = channel.getExitStatus();
            String output = outputStream.toString(StandardCharsets.UTF_8);
            String error = errorStream.toString(StandardCharsets.UTF_8);

            CommandResult result = CommandResult.builder()
                    .success(exitCode == 0)
                    .output(output)
                    .error(error)
                    .exitCode(exitCode)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();

            if (exitCode != 0) {
                log.warn("Command failed with exit code {}: {} on {}",
                        exitCode, command, serverName);
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException(
                    "Command execution was interrupted",
                    command,
                    serverName,
                    -1
            );
        } catch (Exception e) {
            if (e instanceof SSHConnectionException) {
                throw (SSHConnectionException) e;
            } else if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else if (e instanceof ConfigurationException) {
                throw (ConfigurationException) e;
            }
            log.error("Unexpected error executing command: {} on {}", command, serverName, e);
            throw new CommandExecutionException(
                    "Unexpected error: " + e.getMessage(),
                    command,
                    serverName,
                    -1
            );
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            connectionPool.returnConnection(serverName, session);
        }
    }

    public void uploadFile(String serverName, String localPath, String remotePath) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = connectionPool.getConnection(serverName);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            // Check if local file exists
            File localFile = new File(localPath);
            if (!localFile.exists()) {
                throw new ResourceNotFoundException("Local file", localPath);
            }

            try {
                sftpChannel.put(localPath, remotePath);
                log.info("File uploaded successfully from {} to {}", localPath, remotePath);
            } catch (SftpException e) {
                throw new FileOperationException(
                        "Failed to upload file: " + e.getMessage(),
                        new File(remotePath).toPath(),
                        FileOperationException.OperationType.WRITE,
                        serverName,
                        e
                );
            }

        } catch (Exception e) {
            if (e instanceof FileOperationException) {
                throw (FileOperationException) e;
            } else if (e instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e;
            }
            log.error("Failed to upload file", e);
            throw new FileOperationException(
                    "File upload failed: " + e.getMessage(),
                    new File(remotePath).toPath(),
                    FileOperationException.OperationType.WRITE,
                    serverName,
                    e
            );
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            connectionPool.returnConnection(serverName, session);
        }
    }

    private String prepareSudoCommand(String serverName, String command, boolean useSudo) {
        if (!useSudo) {
            return command;
        }

        SSHProperties.ServerConfig config = sshProperties.getServers().get(serverName);
        if (config != null && config.getSudoPassword() != null) {
            return String.format("echo '%s' | sudo -S %s",
                    config.getSudoPassword(), command);
        }

        return "sudo " + command;
    }
}
package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.config.SSHProperties;
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

    @Retryable(value = JSchException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CommandResult executeCommand(String serverName, String command, boolean useSudo) {
        Session session = null;
        ChannelExec channel = null;

        try {
            session = connectionPool.getConnection(serverName);
            channel = (ChannelExec) session.openChannel("exec");

            String finalCommand = prepareSudoCommand(serverName, command, useSudo);
            channel.setCommand(finalCommand);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            channel.setOutputStream(outputStream);
            channel.setErrStream(errorStream);

            channel.connect(sshProperties.getCommandTimeout());

            // Wait for command completion
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            int exitCode = channel.getExitStatus();
            String output = outputStream.toString(StandardCharsets.UTF_8);
            String error = errorStream.toString(StandardCharsets.UTF_8);

            if (exitCode == 0) {
                return CommandResult.success(output);
            } else {
                return CommandResult.failure(error.isEmpty() ? output : error);
            }

        } catch (Exception e) {
            log.error("Failed to execute command: {} on server: {}", command, serverName, e);
            return CommandResult.failure(e.getMessage());
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

            sftpChannel.put(localPath, remotePath);
            log.info("File uploaded successfully from {} to {}", localPath, remotePath);

        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            connectionPool.returnConnection(serverName, session);
        }
    }

    public void downloadFile(String serverName, String remotePath, String localPath) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = connectionPool.getConnection(serverName);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.get(remotePath, localPath);
            log.info("File downloaded successfully from {} to {}", remotePath, localPath);

        } catch (Exception e) {
            log.error("Failed to download file", e);
            throw new RuntimeException("File download failed: " + e.getMessage());
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
            return String.format("echo '%s' | sudo -S %s", config.getSudoPassword(), command);
        }

        return "sudo " + command;
    }
}
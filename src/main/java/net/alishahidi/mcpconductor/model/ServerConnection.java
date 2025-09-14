package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConnection {
    private String serverName;
    private String host;
    private int port;
    private String username;
    private ConnectionType type;
    private ConnectionStatus status;
    private LocalDateTime connectedAt;
    private LocalDateTime lastUsedAt;
    private int activeCommands;

    public enum ConnectionType {
        SSH, DOCKER, KUBERNETES
    }

    public enum ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }
}
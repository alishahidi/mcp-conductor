package net.alishahidi.mcpconductor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ssh")
public class SSHProperties {
    private String defaultHost = "localhost";
    private int defaultPort = 22;
    private String defaultUsername = "root";
    private String privateKeyPath;
    private String privateKeyPassphrase;
    private String defaultPassword;
    private int connectionTimeout = 10000;
    private int commandTimeout = 30000;
    private int maxPoolSize = 10;
    private Map<String, ServerConfig> servers = new HashMap<>();

    @Data
    public static class ServerConfig {
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String privateKeyPath;
        private String sudoPassword;
    }
}
package net.alishahidi.mcpconductor.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import net.alishahidi.mcpconductor.config.SSHProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class SSHConnectionPool {
    
    private final JSch jsch;
    private final SSHProperties properties;
    private final Map<String, Session> connections = new ConcurrentHashMap<>();
    
    public Session getConnection(String serverName) throws Exception {
        Session session = connections.get(serverName);
        
        if (session == null || !session.isConnected()) {
            session = createSession(serverName);
            connections.put(serverName, session);
        }
        
        return session;
    }
    
    private Session createSession(String serverName) throws Exception {
        SSHProperties.ServerConfig config = properties.getServers().get(serverName);
        
        String host = config != null ? config.getHost() : properties.getDefaultHost();
        int port = config != null ? config.getPort() : properties.getDefaultPort();
        String username = config != null ? config.getUsername() : properties.getDefaultUsername();
        String password = config != null ? config.getPassword() : properties.getDefaultPassword();
        String keyPath = config != null && config.getPrivateKeyPath() != null ? 
                        config.getPrivateKeyPath() : properties.getPrivateKeyPath();
        
        Session session = jsch.getSession(username, host, port);
        
        if (keyPath != null && !keyPath.isEmpty()) {
            jsch.addIdentity(keyPath, properties.getPrivateKeyPassphrase());
        } else if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }
        
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(properties.getConnectionTimeout());
        
        log.info("SSH connection established to {}@{}:{}", username, host, port);
        return session;
    }
    
    public void returnConnection(String serverName, Session session) {
        // Connection pooling - keep the session alive for reuse
        if (session != null && session.isConnected()) {
            connections.put(serverName, session);
        }
    }
    
    public void closeAll() {
        connections.values().forEach(session -> {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        });
        connections.clear();
    }
}
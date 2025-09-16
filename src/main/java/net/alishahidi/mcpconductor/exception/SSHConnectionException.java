package net.alishahidi.mcpconductor.exception;

public class SSHConnectionException extends RuntimeException {

    private final String host;
    private final int port;
    private final String username;

    public SSHConnectionException(String message) {
        super(message);
        this.host = null;
        this.port = -1;
        this.username = null;
    }

    public SSHConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.host = null;
        this.port = -1;
        this.username = null;
    }

    public SSHConnectionException(String message, String host, int port, String username, Throwable cause) {
        super(String.format("%s (host: %s:%d, user: %s)", message, host, port, username), cause);
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }
}
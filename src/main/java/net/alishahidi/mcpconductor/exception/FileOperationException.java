package net.alishahidi.mcpconductor.exception;

import java.nio.file.Path;

public class FileOperationException extends RuntimeException {

    private final Path path;
    private final OperationType operation;
    private final String serverName;

    public enum OperationType {
        READ, WRITE, DELETE, CREATE, MOVE, COPY, CHMOD, CHOWN, LIST
    }

    public FileOperationException(String message, Path path, OperationType operation) {
        super(String.format("[%s] %s - Path: %s", operation, message, path));
        this.path = path;
        this.operation = operation;
        this.serverName = null;
    }

    public FileOperationException(String message, Path path, OperationType operation, String serverName, Throwable cause) {
        super(String.format("[%s on %s] %s - Path: %s", operation, serverName, message, path), cause);
        this.path = path;
        this.operation = operation;
        this.serverName = serverName;
    }

    // Getters
    public Path getPath() { return path; }
    public OperationType getOperation() { return operation; }
    public String getServerName() { return serverName; }
}
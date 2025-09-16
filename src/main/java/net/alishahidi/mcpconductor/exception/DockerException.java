package net.alishahidi.mcpconductor.exception;

public class DockerException extends RuntimeException {

    private final String containerId;
    private final String operation;
    private final ErrorCode errorCode;

    public enum ErrorCode {
        CONTAINER_NOT_FOUND,
        IMAGE_NOT_FOUND,
        PULL_FAILED,
        START_FAILED,
        STOP_FAILED,
        NETWORK_ERROR,
        PERMISSION_DENIED,
        RESOURCE_EXHAUSTED
    }

    public DockerException(String message, String operation) {
        super(message);
        this.containerId = null;
        this.operation = operation;
        this.errorCode = ErrorCode.NETWORK_ERROR;
    }

    public DockerException(String message, String containerId, String operation, ErrorCode errorCode) {
        super(String.format("[Docker %s] %s - Container: %s", operation, message, containerId));
        this.containerId = containerId;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    public DockerException(String message, Throwable cause, String operation) {
        super(message, cause);
        this.containerId = null;
        this.operation = operation;
        this.errorCode = ErrorCode.NETWORK_ERROR;
    }

    // Getters
    public String getContainerId() { return containerId; }
    public String getOperation() { return operation; }
    public ErrorCode getErrorCode() { return errorCode; }
}
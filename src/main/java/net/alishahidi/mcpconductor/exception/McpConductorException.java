package net.alishahidi.mcpconductor.exception;

import java.time.LocalDateTime;

public abstract class McpConductorException extends RuntimeException {

    private final String errorCode;
    private final LocalDateTime timestamp;

    protected McpConductorException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    protected McpConductorException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public abstract String getErrorType();
}
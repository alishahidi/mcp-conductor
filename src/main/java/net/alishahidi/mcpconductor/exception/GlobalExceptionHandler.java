// Enhanced GlobalExceptionHandler.java with comprehensive error handling
package net.alishahidi.mcpconductor.exception;

import net.alishahidi.mcpconductor.util.ResponseFormatter;
import net.alishahidi.mcpconductor.security.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ResponseFormatter responseFormatter;
    private final AuditLogger auditLogger;
    private final MeterRegistry meterRegistry;

    @ExceptionHandler(CommandExecutionException.class)
    public ResponseEntity<String> handleCommandExecutionException(
            CommandExecutionException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Command execution failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.command_execution")
                .tag("server", ex.getServerName() != null ? ex.getServerName() : "unknown")
                .tag("exit_code", String.valueOf(ex.getExitCode()))
                .register(meterRegistry)
                .increment();

        // Audit log
        auditLogger.logSecurityEvent("COMMAND_EXECUTION_ERROR",
                String.format("Command: %s, Server: %s, Exit Code: %d, Error: %s",
                        ex.getCommand(), ex.getServerName(), ex.getExitCode(), ex.getMessage()));

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("command", ex.getCommand());
        errorDetails.put("server", ex.getServerName());
        errorDetails.put("exitCode", ex.getExitCode());
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());

        String response = responseFormatter.formatMap(errorDetails,
                "Command execution failed: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SSHConnectionException.class)
    public ResponseEntity<String> handleSSHConnectionException(
            SSHConnectionException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] SSH connection failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.ssh_connection")
                .tag("host", ex.getHost() != null ? ex.getHost() : "unknown")
                .tag("port", String.valueOf(ex.getPort()))
                .register(meterRegistry)
                .increment();

        // Audit log for security
        auditLogger.logSecurityEvent("SSH_CONNECTION_FAILURE",
                String.format("Host: %s:%d, User: %s, Error: %s",
                        ex.getHost(), ex.getPort(), ex.getUsername(), ex.getMessage()));

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("host", ex.getHost());
        errorDetails.put("port", ex.getPort());
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("suggestion", "Please check server connectivity and credentials");

        String response = responseFormatter.formatMap(errorDetails,
                "SSH connection failed: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(DockerException.class)
    public ResponseEntity<String> handleDockerException(
            DockerException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Docker operation failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.docker")
                .tag("operation", ex.getOperation() != null ? ex.getOperation() : "unknown")
                .tag("error_code", ex.getErrorCode().toString())
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("operation", ex.getOperation());
        errorDetails.put("containerId", ex.getContainerId());
        errorDetails.put("errorCode", ex.getErrorCode());
        errorDetails.put("timestamp", LocalDateTime.now());

        HttpStatus status = mapDockerErrorToHttpStatus(ex.getErrorCode());

        String response = responseFormatter.formatMap(errorDetails,
                "Docker operation failed: " + ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<String> handleFileOperationException(
            FileOperationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] File operation failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.file_operation")
                .tag("operation", ex.getOperation().toString())
                .tag("server", ex.getServerName() != null ? ex.getServerName() : "unknown")
                .register(meterRegistry)
                .increment();

        // Audit log for file operations
        auditLogger.logFileOperation(ex.getServerName(),
                ex.getOperation().toString(),
                ex.getPath() != null ? ex.getPath().toString() : "unknown");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("operation", ex.getOperation());
        errorDetails.put("path", ex.getPath());
        errorDetails.put("server", ex.getServerName());
        errorDetails.put("timestamp", LocalDateTime.now());

        String response = responseFormatter.formatMap(errorDetails,
                "File operation failed: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GitOperationException.class)
    public ResponseEntity<String> handleGitOperationException(
            GitOperationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Git operation failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.git_operation")
                .tag("operation", ex.getOperation().toString())
                .tag("repository", ex.getRepository() != null ? "present" : "unknown")
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("operation", ex.getOperation());
        errorDetails.put("repository", ex.getRepository());
        errorDetails.put("branch", ex.getBranch());
        errorDetails.put("timestamp", LocalDateTime.now());

        String response = responseFormatter.formatMap(errorDetails,
                "Git operation failed: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServiceManagementException.class)
    public ResponseEntity<String> handleServiceManagementException(
            ServiceManagementException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Service management failed: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.service_management")
                .tag("service", ex.getServiceName())
                .tag("operation", ex.getOperation().toString())
                .tag("server", ex.getServerName() != null ? ex.getServerName() : "unknown")
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("service", ex.getServiceName());
        errorDetails.put("operation", ex.getOperation());
        errorDetails.put("server", ex.getServerName());
        errorDetails.put("timestamp", LocalDateTime.now());

        String response = responseFormatter.formatMap(errorDetails,
                "Service management failed: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded for client: {}", ex.getClientId());

        // Record metrics
        Counter.builder("exception.rate_limit")
                .tag("client", ex.getClientId())
                .register(meterRegistry)
                .increment();

        // Audit log
        auditLogger.logSecurityEvent("RATE_LIMIT_EXCEEDED",
                String.format("Client: %s, Limit: %d", ex.getClientId(), ex.getLimit()));

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("clientId", ex.getClientId());
        errorDetails.put("limit", ex.getLimit());
        errorDetails.put("resetInMs", ex.getResetTimeMillis());
        errorDetails.put("retryAfter", ex.getResetTimeMillis() / 1000);

        String response = responseFormatter.formatMap(errorDetails,
                "Rate limit exceeded. Please retry after " + (ex.getResetTimeMillis() / 1000) + " seconds");

        ResponseEntity<String> responseEntity = ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", String.valueOf(ex.getLimit()))
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + ex.getResetTimeMillis()))
                .header("Retry-After", String.valueOf(ex.getResetTimeMillis() / 1000))
                .body(response);

        return responseEntity;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        log.warn("Validation failed: {}", ex.getMessage());

        // Record metrics
        Counter.builder("exception.validation")
                .tag("field", ex.getField() != null ? ex.getField() : "unknown")
                .tag("rule", ex.getValidationRule() != null ? ex.getValidationRule() : "general")
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("field", ex.getField());
        errorDetails.put("rejectedValue", ex.getRejectedValue());
        errorDetails.put("rule", ex.getValidationRule());
        errorDetails.put("message", ex.getMessage());

        String response = responseFormatter.formatMap(errorDetails,
                "Validation failed");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} - {}", ex.getResourceType(), ex.getResourceId());

        // Record metrics
        Counter.builder("exception.resource_not_found")
                .tag("resource_type", ex.getResourceType())
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("resourceType", ex.getResourceType());
        errorDetails.put("resourceId", ex.getResourceId());
        errorDetails.put("timestamp", LocalDateTime.now());

        String response = responseFormatter.formatMap(errorDetails,
                ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<String> handleConfigurationException(
            ConfigurationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Configuration error: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        Counter.builder("exception.configuration")
                .tag("config_key", ex.getConfigKey() != null ? ex.getConfigKey() : "unknown")
                .tag("config_file", ex.getConfigFile() != null ? ex.getConfigFile() : "unknown")
                .register(meterRegistry)
                .increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("configKey", ex.getConfigKey());
        errorDetails.put("configFile", ex.getConfigFile());
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("suggestion", "Please check your configuration settings");

        String response = responseFormatter.formatMap(errorDetails,
                "Configuration error: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Method argument validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Record metrics
        meterRegistry.counter("exception.method_argument_validation").increment();

        String response = responseFormatter.formatMap(errors, "Validation errors");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {} - {}", request.getRequestURI(), ex.getMessage());

        // Record metrics and audit
        meterRegistry.counter("exception.access_denied").increment();
        auditLogger.logSecurityEvent("ACCESS_DENIED",
                String.format("Path: %s, Message: %s", request.getRequestURI(), ex.getMessage()));

        String response = responseFormatter.formatError(
                "Access denied: You don't have permission to access this resource");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed: {}", request.getRemoteAddr());

        // Record metrics and audit
        meterRegistry.counter("exception.bad_credentials").increment();
        auditLogger.logSecurityEvent("AUTHENTICATION_FAILED",
                String.format("IP: %s", request.getRemoteAddr()));

        String response = responseFormatter.formatError(
                "Authentication failed: Invalid credentials");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Invalid argument: {}", ex.getMessage());

        // Record metrics
        meterRegistry.counter("exception.illegal_argument").increment();

        String response = responseFormatter.formatError(
                "Invalid argument: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        // Record metrics
        meterRegistry.counter("exception.no_handler_found").increment();

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("method", ex.getHttpMethod());
        errorDetails.put("path", ex.getRequestURL());
        errorDetails.put("timestamp", LocalDateTime.now());

        String response = responseFormatter.formatMap(errorDetails,
                "Endpoint not found");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Runtime error: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        meterRegistry.counter("exception.runtime").increment();

        // Check if it's a critical error
        boolean isCritical = isCriticalError(ex);
        if (isCritical) {
            // Send alert for critical errors
            sendCriticalErrorAlert(errorId, ex, request);
        }

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());

        String response = responseFormatter.formatMap(errorDetails,
                "Internal error occurred. Error ID: " + errorId);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Error.class)
    public ResponseEntity<String> handleError(
            Error error,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Critical error: {}", errorId, error.getMessage(), error);

        // Record metrics
        meterRegistry.counter("exception.critical_error").increment();

        // Send alert for critical errors
        sendCriticalErrorAlert(errorId, new RuntimeException(error), request);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", "Critical system error occurred. Error ID: " + errorId);

        String response = responseFormatter.formatMap(errorDetails,
                "Critical system error");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();
        log.error("[{}] Unexpected error: {}", errorId, ex.getMessage(), ex);

        // Record metrics
        meterRegistry.counter("exception.unexpected").increment();

        // Send alert for unexpected errors
        sendCriticalErrorAlert(errorId, ex, request);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorId", errorId);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", "An unexpected error occurred. Please contact support with error ID: " + errorId);

        String response = responseFormatter.formatMap(errorDetails,
                "Unexpected error occurred");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String generateErrorId() {
        return UUID.randomUUID().toString();
    }

    private HttpStatus mapDockerErrorToHttpStatus(DockerException.ErrorCode errorCode) {
        return switch (errorCode) {
            case CONTAINER_NOT_FOUND, IMAGE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case RESOURCE_EXHAUSTED -> HttpStatus.INSUFFICIENT_STORAGE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private boolean isCriticalError(Exception ex) {
        // Check for critical exceptions in the cause chain
        Throwable cause = ex.getCause();
        if (cause instanceof OutOfMemoryError || cause instanceof StackOverflowError) {
            return true;
        }
        
        // Check message for critical keywords
        return ex.getMessage() != null && (
                ex.getMessage().contains("database") ||
                        ex.getMessage().contains("connection pool") ||
                        ex.getMessage().contains("critical") ||
                        ex.getMessage().contains("OutOfMemoryError") ||
                        ex.getMessage().contains("StackOverflowError")
        );
    }

    private void sendCriticalErrorAlert(String errorId, Exception ex, HttpServletRequest request) {
        try {
            Map<String, Object> alertData = Map.of(
                    "errorId", errorId,
                    "exception", ex.getClass().getSimpleName(),
                    "message", ex.getMessage() != null ? ex.getMessage() : "No message",
                    "path", request.getRequestURI(),
                    "method", request.getMethod(),
                    "timestamp", LocalDateTime.now()
            );

            // Log critical error for monitoring systems
            log.error("CRITICAL_ERROR: {}", alertData);

            // Audit log
            auditLogger.logSecurityEvent("CRITICAL_ERROR",
                    String.format("Error ID: %s, Exception: %s", errorId, ex.getClass().getSimpleName()));

            // Here you would integrate with alerting systems like PagerDuty, Slack, etc.
            // Example: alertService.sendCriticalAlert(alertData);

        } catch (Exception alertEx) {
            log.error("Failed to send critical error alert", alertEx);
        }
    }
}
package net.alishahidi.mcpconductor.exception;

public class ServiceManagementException extends RuntimeException {

    private final String serviceName;
    private final ServiceOperation operation;
    private final String serverName;

    public enum ServiceOperation {
        START, STOP, RESTART, ENABLE, DISABLE, STATUS, RELOAD
    }

    public ServiceManagementException(String message, String serviceName, ServiceOperation operation) {
        super(String.format("[Service %s] %s - Service: %s", operation, message, serviceName));
        this.serviceName = serviceName;
        this.operation = operation;
        this.serverName = null;
    }

    public ServiceManagementException(String message, String serviceName, ServiceOperation operation, String serverName) {
        super(String.format("[Service %s on %s] %s - Service: %s", operation, serverName, message, serviceName));
        this.serviceName = serviceName;
        this.operation = operation;
        this.serverName = serverName;
    }

    // Getters
    public String getServiceName() { return serviceName; }
    public ServiceOperation getOperation() { return operation; }
    public String getServerName() { return serverName; }
}
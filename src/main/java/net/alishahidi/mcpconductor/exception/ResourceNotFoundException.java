package net.alishahidi.mcpconductor.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    // Getters
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}
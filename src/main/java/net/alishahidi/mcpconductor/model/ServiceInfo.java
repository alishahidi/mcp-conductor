package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfo {
    private String name;
    private String description;
    private ServiceStatus status;
    private boolean enabled;
    private String mainPid;
    private String memory;
    private String cpuTime;
    private String startTime;

    public enum ServiceStatus {
        RUNNING, STOPPED, FAILED, UNKNOWN, STARTING, STOPPING
    }
}
package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfo {
    private String hostname;
    private String operatingSystem;
    private String kernelVersion;
    private String architecture;
    private CpuInfo cpu;
    private MemoryInfo memory;
    private DiskInfo disk;
    private NetworkInfo network;
    private Long uptime;
    private Double loadAverage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CpuInfo {
        private int cores;
        private String model;
        private double usagePercent;
        private double temperature;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {
        private long totalBytes;
        private long usedBytes;
        private long freeBytes;
        private long availableBytes;
        private double usagePercent;
        private long swapTotalBytes;
        private long swapUsedBytes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskInfo {
        private String filesystem;
        private long totalBytes;
        private long usedBytes;
        private long availableBytes;
        private double usagePercent;
        private String mountPoint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkInfo {
        private String interfaceName;
        private String ipAddress;
        private String macAddress;
        private long rxBytes;
        private long txBytes;
        private long rxPackets;
        private long txPackets;
    }
}
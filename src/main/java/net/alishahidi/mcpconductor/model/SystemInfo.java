package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfo {
    private String hostname;
    private String operatingSystem;
    private String kernelVersion;
    private String architecture;
    private String uptime;
    private String memoryInfo;
    private String diskUsage;
    private String loadAverage;
    private Integer cpuCores;
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("hostname", hostname);
        map.put("operatingSystem", operatingSystem);
        map.put("kernelVersion", kernelVersion);
        map.put("architecture", architecture);
        map.put("uptime", uptime);
        map.put("memoryInfo", memoryInfo);
        map.put("diskUsage", diskUsage);
        map.put("loadAverage", loadAverage);
        map.put("cpuCores", cpuCores);
        return map;
    }

}
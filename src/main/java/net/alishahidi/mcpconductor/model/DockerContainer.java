package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerContainer {
    private String id;
    private List<String> names;
    private String image;
    private String imageId;
    private String command;
    private Long created;
    private String status;
    private String state;
    private List<String> ports;
    private Map<String, String> labels;
    private String networkMode;
    private List<String> mounts;
}

package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageInfo {
    private String name;
    private String version;
    private String description;
    private String architecture;
    private long size;
    private String repository;
    private boolean installed;
    private boolean upgradable;
    private String currentVersion;
    private String availableVersion;
    private PackageManager packageManager;

    public enum PackageManager {
        APT, YUM, DNF, PACMAN, BREW, SNAP, FLATPAK
    }
}
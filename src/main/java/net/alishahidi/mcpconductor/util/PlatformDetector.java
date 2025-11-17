package net.alishahidi.mcpconductor.util;

import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

/**
 * Cross-platform detection utility for determining operating system and platform-specific configurations.
 * Supports Linux, Windows, and macOS for comprehensive cross-platform compatibility.
 */
@Component
@Slf4j
@Getter
public class PlatformDetector {

    public enum Platform {
        LINUX,
        WINDOWS,
        MACOS,
        UNKNOWN
    }

    public enum PackageManager {
        APT,        // Debian/Ubuntu
        YUM,        // RHEL/CentOS 6-7
        DNF,        // Fedora/RHEL 8+
        PACMAN,     // Arch Linux
        ZYPPER,     // openSUSE
        BREW,       // macOS/Linux Homebrew
        CHOCOLATEY, // Windows
        WINGET,     // Windows
        SCOOP,      // Windows
        UNKNOWN
    }

    private Platform currentPlatform;
    private String osName;
    private String osVersion;
    private String osArch;

    @PostConstruct
    public void detectPlatform() {
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        osArch = System.getProperty("os.arch");

        String osLower = osName.toLowerCase();

        if (osLower.contains("win")) {
            currentPlatform = Platform.WINDOWS;
        } else if (osLower.contains("mac") || osLower.contains("darwin")) {
            currentPlatform = Platform.MACOS;
        } else if (osLower.contains("nix") || osLower.contains("nux") || osLower.contains("aix")) {
            currentPlatform = Platform.LINUX;
        } else {
            currentPlatform = Platform.UNKNOWN;
        }

        log.info("Platform detected: {} (OS: {}, Version: {}, Arch: {})",
                currentPlatform, osName, osVersion, osArch);
    }

    public boolean isLinux() {
        return currentPlatform == Platform.LINUX;
    }

    public boolean isWindows() {
        return currentPlatform == Platform.WINDOWS;
    }

    public boolean isMacOS() {
        return currentPlatform == Platform.MACOS;
    }

    public boolean isUnix() {
        return isLinux() || isMacOS();
    }

    /**
     * Get platform-specific line separator
     */
    public String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * Get platform-specific path separator
     */
    public String getPathSeparator() {
        return System.getProperty("path.separator");
    }

    /**
     * Get platform-specific file separator
     */
    public String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * Get platform-specific shell command
     */
    public String getShellCommand() {
        return switch (currentPlatform) {
            case WINDOWS -> "cmd.exe";
            case LINUX, MACOS -> "/bin/bash";
            default -> "sh";
        };
    }

    /**
     * Get platform-specific shell flag for executing commands
     */
    public String getShellFlag() {
        return switch (currentPlatform) {
            case WINDOWS -> "/c";
            case LINUX, MACOS -> "-c";
            default -> "-c";
        };
    }

    /**
     * Get default Docker host for the platform
     */
    public String getDefaultDockerHost() {
        return switch (currentPlatform) {
            case WINDOWS -> "npipe:////./pipe/docker_engine";
            case LINUX, MACOS -> "unix:///var/run/docker.sock";
            default -> "unix:///var/run/docker.sock";
        };
    }

    /**
     * Get process list command for the platform
     */
    public String getProcessListCommand(int limit) {
        return switch (currentPlatform) {
            case WINDOWS -> limit > 0 ?
                String.format("powershell -Command \"Get-Process | Sort-Object CPU -Descending | Select-Object -First %d\"", limit) :
                "powershell -Command \"Get-Process | Sort-Object CPU -Descending\"";
            case LINUX, MACOS -> limit > 0 ?
                String.format("ps aux --sort=-%scpu | head -%d", "%", limit + 1) :
                "ps aux --sort=-%cpu";
            default -> "ps aux";
        };
    }

    /**
     * Get disk usage command for the platform
     */
    public String getDiskUsageCommand() {
        return switch (currentPlatform) {
            case WINDOWS -> "powershell -Command \"Get-PSDrive -PSProvider FileSystem | Select-Object Name, Used, Free, @{Name='Size';Expression={$_.Used+$_.Free}}\"";
            case LINUX, MACOS -> "df -h";
            default -> "df";
        };
    }

    /**
     * Get memory usage command for the platform
     */
    public String getMemoryUsageCommand() {
        return switch (currentPlatform) {
            case WINDOWS -> "powershell -Command \"Get-CimInstance Win32_OperatingSystem | Select-Object TotalVisibleMemorySize, FreePhysicalMemory\"";
            case LINUX, MACOS -> "free -h";
            default -> "free";
        };
    }

    /**
     * Get network info command for the platform
     */
    public String getNetworkInfoCommand() {
        return switch (currentPlatform) {
            case WINDOWS -> "ipconfig /all";
            case LINUX -> "ip addr show";
            case MACOS -> "ifconfig";
            default -> "ifconfig";
        };
    }

    /**
     * Get system uptime command for the platform
     */
    public String getUptimeCommand() {
        return switch (currentPlatform) {
            case WINDOWS -> "powershell -Command \"(Get-CimInstance Win32_OperatingSystem).LastBootUpTime\"";
            case LINUX, MACOS -> "uptime";
            default -> "uptime";
        };
    }

    /**
     * Get service status command for the platform
     */
    public String getServiceStatusCommand(String serviceName) {
        return switch (currentPlatform) {
            case WINDOWS -> String.format("sc query \"%s\"", serviceName);
            case LINUX -> String.format("systemctl status %s", serviceName);
            case MACOS -> String.format("launchctl list | grep %s", serviceName);
            default -> String.format("service %s status", serviceName);
        };
    }

    /**
     * Detect available package manager on the system
     */
    public PackageManager detectPackageManager(String hint) {
        // If hint is provided and valid, use it
        if (hint != null && !hint.isBlank()) {
            try {
                return PackageManager.valueOf(hint.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid package manager hint: {}, attempting auto-detection", hint);
            }
        }

        // Platform-specific detection
        return switch (currentPlatform) {
            case WINDOWS -> PackageManager.WINGET; // Default to winget on Windows
            case MACOS -> PackageManager.BREW;     // Default to brew on macOS
            case LINUX -> detectLinuxPackageManager();
            default -> PackageManager.UNKNOWN;
        };
    }

    private PackageManager detectLinuxPackageManager() {
        // Check for common Linux package managers
        // This would ideally execute commands to check, but for now return apt as most common
        // In production, you'd check: which apt-get, which yum, etc.
        return PackageManager.APT;
    }

    /**
     * Normalize path for the current platform
     */
    public String normalizePath(String path) {
        if (path == null) {
            return null;
        }

        if (isWindows()) {
            // Convert forward slashes to backslashes on Windows
            return path.replace("/", "\\");
        } else {
            // Convert backslashes to forward slashes on Unix
            return path.replace("\\", "/");
        }
    }

    /**
     * Check if a path is absolute for the current platform
     */
    public boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        if (isWindows()) {
            // Windows: C:\path or \\network\path
            return path.matches("^[a-zA-Z]:\\\\.*") || path.startsWith("\\\\");
        } else {
            // Unix: /path
            return path.startsWith("/");
        }
    }

    /**
     * Get platform information as a map
     */
    public java.util.Map<String, String> getPlatformInfo() {
        return java.util.Map.of(
                "platform", currentPlatform.name(),
                "osName", osName,
                "osVersion", osVersion,
                "osArch", osArch,
                "javaVersion", System.getProperty("java.version"),
                "javaVendor", System.getProperty("java.vendor")
        );
    }
}

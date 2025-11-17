package net.alishahidi.mcpconductor.util;

import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

/**
 * Platform detection utility for the MCP Conductor server itself (not target servers).
 *
 * The MCP Conductor server is a cross-platform Spring Boot application that can run on:
 * - Linux (Ubuntu, CentOS, Fedora, etc.)
 * - Windows (Windows 10/11, Windows Server)
 * - macOS (Intel and Apple Silicon)
 *
 * This detector is used for:
 * - Docker host configuration (Unix socket vs Windows named pipe)
 * - Platform-specific server settings
 *
 * NOTE: Target servers managed by MCP Conductor are Linux-only.
 * All SSH commands execute on Linux systems regardless of where the MCP server runs.
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

    /**
     * Linux package managers (for target server management).
     * These are used by PackageService to manage Linux servers via SSH.
     */
    public enum PackageManager {
        APT,        // Debian/Ubuntu
        YUM,        // RHEL/CentOS 6-7
        DNF,        // Fedora/RHEL 8+
        PACMAN,     // Arch Linux
        ZYPPER,     // openSUSE
        BREW,       // Homebrew on Linux
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
     * NOTE: The methods below are NOT used for target server management.
     * All target servers are Linux-only and use standard Linux commands.
     * These methods are kept for potential future local operations on the MCP server itself.
     */

    /**
     * Get process list command (Linux only - for target servers)
     */
    public String getProcessListCommand(int limit) {
        // Target servers are always Linux
        return limit > 0 ?
            String.format("ps aux --sort=-%scpu | head -%d", "%", limit + 1) :
            "ps aux --sort=-%cpu";
    }

    /**
     * Get disk usage command (Linux only - for target servers)
     */
    public String getDiskUsageCommand() {
        // Target servers are always Linux
        return "df -h";
    }

    /**
     * Get memory usage command (Linux only - for target servers)
     */
    public String getMemoryUsageCommand() {
        // Target servers are always Linux
        return "free -h";
    }

    /**
     * Get network info command (Linux only - for target servers)
     */
    public String getNetworkInfoCommand() {
        // Target servers are always Linux
        return "ip addr show";
    }

    /**
     * Get system uptime command (Linux only - for target servers)
     */
    public String getUptimeCommand() {
        // Target servers are always Linux
        return "uptime";
    }

    /**
     * Get service status command (Linux only - for target servers)
     */
    public String getServiceStatusCommand(String serviceName) {
        // Target servers are always Linux with systemd
        return String.format("systemctl status %s", serviceName);
    }

    /**
     * Detect available package manager on Linux target servers.
     *
     * NOTE: This is for target servers being managed, not the MCP server itself.
     * All target servers are Linux-only.
     */
    public PackageManager detectPackageManager(String hint) {
        // If hint is provided and valid, use it
        if (hint != null && !hint.isBlank()) {
            try {
                return PackageManager.valueOf(hint.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid package manager hint: {}, defaulting to apt", hint);
            }
        }

        // Default to apt (most common for Ubuntu/Debian)
        // In practice, the package manager is explicitly specified per server
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

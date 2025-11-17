package net.alishahidi.mcpconductor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@Slf4j
public class PathValidator {

    private static final Set<String> RESTRICTED_PATHS = Set.of(
            "/etc/passwd",
            "/etc/shadow",
            "/etc/sudoers",
            "/etc/ssh/sshd_config",
            "/root/.ssh",
            "/proc/kcore",
            "/dev/mem",
            "/dev/kmem",
            "/sys",
            "/boot/grub"
    );

    private final Set<String> allowedBasePaths;
    private final Set<String> blockedPaths;

    public PathValidator(@Value("${security.path.allowed:}") List<String> allowedPaths,
                         @Value("${security.path.blocked:}") List<String> blockedPathsList) {
        this.allowedBasePaths = new HashSet<>(allowedPaths.isEmpty() ?
                List.of("/home", "/var", "/opt", "/tmp", "/usr/local") : allowedPaths);
        this.blockedPaths = new HashSet<>(blockedPathsList);
        this.blockedPaths.addAll(RESTRICTED_PATHS);
    }

    public boolean isValidPath(String pathStr) {
        if (pathStr == null || pathStr.trim().isEmpty()) {
            return false;
        }

        try {
            // Normalize the path
            Path path = Paths.get(pathStr).normalize();
            String normalizedPath = path.toString();

            // Check for path traversal attempts in the original string
            // Only flag if .. appears outside of initial normalization
            if (pathStr.contains("/../") || pathStr.endsWith("/..") ||
                pathStr.equals("..") || pathStr.startsWith("../")) {
                log.warn("Path traversal attempt detected: {}", pathStr);
                return false;
            }

            // Convert relative paths to absolute for validation
            if (!path.isAbsolute()) {
                // For relative paths, just ensure they don't traverse upward unsafely
                log.debug("Accepting relative path: {}", pathStr);
                return !pathStr.contains("/../") && !pathStr.startsWith("../");
            }

            // Check against blocked paths (for absolute paths)
            for (String blockedPath : blockedPaths) {
                if (normalizedPath.equals(blockedPath) || normalizedPath.startsWith(blockedPath + "/")) {
                    log.warn("Access to restricted path blocked: {}", normalizedPath);
                    return false;
                }
            }

            // If no allowed paths configured or empty, allow all non-blocked absolute paths
            if (allowedBasePaths.isEmpty()) {
                log.debug("No path restrictions configured, allowing: {}", normalizedPath);
                return true;
            }

            // Check if path is within allowed base paths
            boolean isAllowed = allowedBasePaths.stream()
                    .anyMatch(allowedPath ->
                        normalizedPath.equals(allowedPath) ||
                        normalizedPath.startsWith(allowedPath + "/"));

            if (!isAllowed) {
                log.warn("Path outside allowed directories: {}", normalizedPath);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Invalid path format: {}", pathStr, e);
            return false;
        }
    }

    public String normalizePath(String pathStr) {
        if (pathStr == null) {
            return null;
        }

        try {
            return Paths.get(pathStr).normalize().toString();
        } catch (Exception e) {
            log.error("Failed to normalize path: {}", pathStr, e);
            return pathStr;
        }
    }
}
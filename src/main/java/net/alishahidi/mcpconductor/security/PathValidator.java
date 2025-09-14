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
            Path path = Paths.get(pathStr).normalize();
            String normalizedPath = path.toString();

            // Check for path traversal attempts
            if (normalizedPath.contains("..")) {
                log.warn("Path traversal attempt detected: {}", pathStr);
                return false;
            }

            // Check against blocked paths
            if (blockedPaths.stream().anyMatch(normalizedPath::startsWith)) {
                log.warn("Access to restricted path blocked: {}", normalizedPath);
                return false;
            }

            // Check if path is within allowed base paths
            boolean isAllowed = allowedBasePaths.stream()
                    .anyMatch(normalizedPath::startsWith);

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
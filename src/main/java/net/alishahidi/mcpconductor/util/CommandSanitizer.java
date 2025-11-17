package net.alishahidi.mcpconductor.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.List;

/**
 * Utility for sanitizing shell arguments and paths.
 * This class provides helper methods for escaping arguments and detecting path traversal.
 *
 * NOTE: For command validation, use CommandValidator instead.
 * CommandValidator provides comprehensive validation that allows legitimate shell usage
 * while blocking dangerous patterns. This class is for specific sanitization tasks.
 */
@Component
@Slf4j
public class CommandSanitizer {

    private static final Set<String> DANGEROUS_COMMANDS = Set.of(
            "rm", "del", "format", "fdisk", "mkfs", "dd", "shutdown", "reboot", "halt", "poweroff"
    );

    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("eval\\s+"),                      // eval usage
            Pattern.compile("(wget|curl)\\s+.*\\|\\s*(sh|bash)"), // Download and execute
            Pattern.compile("nc\\s+-l"),                      // Netcat listener
            Pattern.compile("bash\\s+-i"),                    // Interactive bash
            Pattern.compile("/etc/(passwd|shadow)"),          // Sensitive files
            Pattern.compile("%2[efEF]"),                      // URL encoded slashes
            Pattern.compile("\\\\x[0-9a-fA-F]{2}")           // Hex encoding
    );

    /**
     * Minimal sanitization - only remove truly dangerous characters.
     * Preserves legitimate shell usage.
     */
    public String sanitizeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }

        String sanitized = command.trim();

        // Remove null bytes (can hide malicious content)
        sanitized = sanitized.replace("\0", "");

        // Remove carriage returns (can hide commands)
        sanitized = sanitized.replace("\r", "");

        log.debug("Command minimal sanitization applied");
        return sanitized;
    }

    /**
     * Check if command contains suspicious patterns.
     * This is a helper method - use CommandValidator.isValid() for full validation.
     */
    public boolean isCommandSafe(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        // Check for suspicious patterns that are never legitimate
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(command).find()) {
                log.warn("Suspicious pattern detected in command: {}", command);
                return false;
            }
        }

        return true;
    }

    public String escapeShellArgument(String argument) {
        if (argument == null) {
            return "";
        }
        
        // Escape single quotes by replacing ' with '\''
        String escaped = argument.replace("'", "'\"'\"'");
        
        // Wrap in single quotes to prevent shell interpretation
        return "'" + escaped + "'";
    }

    public String removeInjectionAttempts(String command) {
        if (command == null) {
            return "";
        }
        
        String cleaned = command;
        
        // Remove command substitution
        cleaned = cleaned.replaceAll("\\$\\([^)]*\\)", "");
        cleaned = cleaned.replaceAll("`[^`]*`", "");
        
        // Remove variable substitution
        cleaned = cleaned.replaceAll("\\$\\{[^}]*\\}", "");
        cleaned = cleaned.replaceAll("\\$[a-zA-Z_][a-zA-Z0-9_]*", "");
        
        // Remove redirections and pipes
        cleaned = cleaned.replaceAll("[|&;><]", " ");
        
        // Remove multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        return cleaned.trim();
    }

    public boolean hasPathTraversal(String path) {
        if (path == null) {
            return false;
        }
        
        return path.contains("../") || 
               path.contains("..\\") || 
               path.contains("/..") || 
               path.contains("\\..") ||
               path.matches(".*\\.{2,}.*");
    }

    public String sanitizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        String sanitized = path.trim();
        
        // Remove path traversal attempts
        sanitized = sanitized.replaceAll("\\.\\./", "");
        sanitized = sanitized.replaceAll("\\\\\\.\\.\\\\", "");
        sanitized = sanitized.replaceAll("/\\./", "/");
        sanitized = sanitized.replaceAll("\\\\\\.\\\\", "\\\\");
        
        // Remove null bytes
        sanitized = sanitized.replace("\0", "");
        
        // Normalize slashes
        sanitized = sanitized.replaceAll("/{2,}", "/");
        sanitized = sanitized.replaceAll("\\\\{2,}", "\\\\");
        
        return sanitized;
    }

    private String removeCommandSubstitution(String command) {
        String result = command;
        
        // Remove $(...) command substitution
        result = result.replaceAll("\\$\\([^)]*\\)", "");
        
        // Remove `...` backtick substitution
        result = result.replaceAll("`[^`]*`", "");
        
        return result;
    }

    private String removeDangerousOperators(String command) {
        String result = command;
        
        // Replace command separators with spaces
        result = result.replaceAll("[;&|]", " ");
        
        // Remove redirection operators
        result = result.replaceAll("[<>]+", " ");
        
        // Remove double operators
        result = result.replaceAll("\\|\\|", " ");
        result = result.replaceAll("&&", " ");
        
        return result;
    }

    private String escapeSpecialCharacters(String command) {
        StringBuilder result = new StringBuilder();
        
        for (char c : command.toCharArray()) {
            if (isSpecialCharacter(c)) {
                result.append("\\").append(c);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    private boolean isSpecialCharacter(char c) {
        return c == '&' || c == '|' || c == ';' || c == '(' || c == ')' || 
               c == '{' || c == '}' || c == '[' || c == ']' || c == '<' || 
               c == '>' || c == '`' || c == '$' || c == '\\';
    }

    private String extractBaseCommand(String command) {
        // Remove path if present
        int lastSlash = Math.max(command.lastIndexOf('/'), command.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            command = command.substring(lastSlash + 1);
        }
        
        // Remove file extension if present
        int dotIndex = command.lastIndexOf('.');
        if (dotIndex > 0) {
            command = command.substring(0, dotIndex);
        }
        
        return command;
    }

    private boolean containsSuspiciousPatterns(String command) {
        String lower = command.toLowerCase();
        
        // Check for suspicious keywords
        String[] suspiciousKeywords = {
            "eval", "exec", "system", "shell_exec", "passthru", 
            "/etc/passwd", "/etc/shadow", "wget", "curl http://",
            "nc -l", "netcat", "bash -i", "sh -i"
        };
        
        for (String keyword : suspiciousKeywords) {
            if (lower.contains(keyword)) {
                log.warn("Suspicious keyword '{}' found in command: {}", keyword, command);
                return true;
            }
        }
        
        // Check for encoding attempts
        if (lower.contains("%") && (lower.contains("2f") || lower.contains("2e"))) {
            log.warn("URL encoding detected in command: {}", command);
            return true;
        }
        
        return false;
    }
}
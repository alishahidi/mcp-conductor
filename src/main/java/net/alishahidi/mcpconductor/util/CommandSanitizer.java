package net.alishahidi.mcpconductor.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.List;

@Component
@Slf4j
public class CommandSanitizer {

    private static final Set<String> DANGEROUS_COMMANDS = Set.of(
            "rm", "del", "format", "fdisk", "mkfs", "dd", "shutdown", "reboot", "halt", "poweroff"
    );

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("[;&|`$(){}\\[\\]<>]"),  // Command separators and substitution
            Pattern.compile("\\$\\([^)]*\\)"),        // Command substitution
            Pattern.compile("`[^`]*`"),               // Backtick substitution
            Pattern.compile("\\$\\{[^}]*\\}"),        // Variable substitution
            Pattern.compile(">>|<<"),                 // Redirection operators
            Pattern.compile("\\|\\|"),                // OR operator
            Pattern.compile("&&"),                    // AND operator
            Pattern.compile("\\\\[xuU][0-9a-fA-F]+") // Unicode escapes
    );

    private static final Set<String> SAFE_CHARACTERS = Set.of(
            "a-z", "A-Z", "0-9", "-", "_", ".", "/", ":", " ", "=", "+"
    );

    public String sanitizeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }

        String sanitized = command.trim();
        
        // Remove null bytes
        sanitized = sanitized.replace("\0", "");
        
        // Remove potential path traversal
        sanitized = sanitized.replaceAll("\\.\\./", "");
        sanitized = sanitized.replaceAll("\\\\\\.\\.\\\\", "");
        
        // Remove command substitution attempts
        sanitized = removeCommandSubstitution(sanitized);
        
        // Remove dangerous operators
        sanitized = removeDangerousOperators(sanitized);
        
        // Escape special characters
        sanitized = escapeSpecialCharacters(sanitized);
        
        log.debug("Command sanitized from '{}' to '{}'", command, sanitized);
        return sanitized;
    }

    public boolean isCommandSafe(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        String normalizedCommand = command.toLowerCase().trim();
        
        // Check for dangerous commands
        String[] commandParts = normalizedCommand.split("\\s+");
        if (commandParts.length > 0) {
            String baseCommand = extractBaseCommand(commandParts[0]);
            if (DANGEROUS_COMMANDS.contains(baseCommand)) {
                log.warn("Dangerous command detected: {}", baseCommand);
                return false;
            }
        }
        
        // Check for injection patterns
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(command).find()) {
                log.warn("Command injection pattern detected in: {}", command);
                return false;
            }
        }
        
        // Check for suspicious patterns
        if (containsSuspiciousPatterns(command)) {
            return false;
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
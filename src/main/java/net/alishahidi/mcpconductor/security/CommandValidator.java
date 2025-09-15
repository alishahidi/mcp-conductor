package net.alishahidi.mcpconductor.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CommandValidator {

    private static final Set<String> DANGEROUS_COMMANDS = Set.of(
            "rm -rf /",
            "rm -rf /*",
            "dd if=/dev/zero",
            "mkfs",
            "format",
            "> /dev/sda",
            "chmod -R 777 /",
            ":(){ :|:& };:"  // Fork bomb
    );

    private static final List<Pattern> DANGEROUS_PATTERNS = List.of(
            Pattern.compile("rm\\s+-rf\\s+/"),
            Pattern.compile("dd\\s+.*of=/dev/"),
            Pattern.compile("mkfs\\.\\w+"),
            Pattern.compile("\\$\\(.*\\)"),  // Command substitution
            Pattern.compile("`.*`"),          // Backtick substitution
            Pattern.compile(".*;\\s*rm\\s+-rf"),
            Pattern.compile(".*&&\\s*rm\\s+-rf"),
            Pattern.compile(".*\\|\\s*sh"),
            Pattern.compile(".*\\|\\s*bash"),
            Pattern.compile(".*;.*"),         // General command chaining with semicolon
            Pattern.compile(".*&&.*")        // General command chaining with &&
    );

    private final Set<String> allowedCommands;
    private final boolean strictMode;

    public CommandValidator(@Value("${security.command.strict-mode:true}") boolean strictMode,
                            @Value("${security.command.allowed:}") List<String> allowedCommandsList) {
        this.strictMode = strictMode;
        this.allowedCommands = new HashSet<>(allowedCommandsList);
    }

    public boolean isValid(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        String normalizedCommand = command.trim().toLowerCase();

        // Check for explicitly dangerous commands
        if (DANGEROUS_COMMANDS.stream().anyMatch(normalizedCommand::contains)) {
            log.warn("Dangerous command blocked: {}", command);
            return false;
        }

        // Check for dangerous patterns
        if (DANGEROUS_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(command).find())) {
            log.warn("Command matches dangerous pattern: {}", command);
            return false;
        }

        // In strict mode, only allow whitelisted commands
        if (strictMode && !allowedCommands.isEmpty()) {
            String baseCommand = extractBaseCommand(command);
            if (!allowedCommands.contains(baseCommand)) {
                log.warn("Command not in whitelist: {}", baseCommand);
                return false;
            }
        }

        return true;
    }

    public String sanitize(String command) {
        if (command == null) {
            return "";
        }

        // Remove potential injection attempts
        String sanitized = command;
        sanitized = sanitized.replaceAll("\\$\\([^)]*\\)", "");  // Remove command substitution
        sanitized = sanitized.replaceAll("`[^`]*`", "");          // Remove backticks
        sanitized = sanitized.replaceAll("\\$\\{[^}]*\\}", "");   // Remove variable substitution
        sanitized = sanitized.replaceAll("[;&|]", " ");           // Replace command separators

        return sanitized.trim();
    }

    private String extractBaseCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length > 0) {
            String baseCmd = parts[0];
            // Remove path if present
            int lastSlash = baseCmd.lastIndexOf('/');
            if (lastSlash >= 0) {
                baseCmd = baseCmd.substring(lastSlash + 1);
            }
            return baseCmd;
        }
        return "";
    }
}
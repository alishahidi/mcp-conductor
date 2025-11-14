package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.SSHService;
import net.alishahidi.mcpconductor.security.CommandValidator;
import net.alishahidi.mcpconductor.security.AuditLogger;
import net.alishahidi.mcpconductor.security.RateLimiter;
import net.alishahidi.mcpconductor.exception.*;
import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.util.ResponseFormatter;
import org.springframework.ai.mcp.server.annotation.McpTool;
import org.springframework.ai.mcp.server.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandExecutionTool {

    private final SSHService sshService;
    private final CommandValidator commandValidator;
    private final AuditLogger auditLogger;
    private final RateLimiter rateLimiter;
    private final ResponseFormatter responseFormatter;

    private static final long COMMAND_TIMEOUT_SECONDS = 300; // 5 minutes

    @McpTool(name = "execute_command",
            description = "Execute a single command on a remote server via SSH with comprehensive error handling and security validation")
    public String executeCommand(
            @McpToolParam(description = "The Linux/Unix command to execute") String command,
            @McpToolParam(description = "The target server identifier") String serverName,
            @McpToolParam(description = "Whether to execute with sudo privileges") boolean useSudo) {

        log.info("Executing command: {} on server: {} (sudo: {})", command, serverName, useSudo);

        try {
            // Rate limiting check
            String clientId = getCurrentClientId();
            if (!rateLimiter.tryConsume(clientId)) {
                throw new RateLimitExceededException(
                        clientId,
                        rateLimiter.getAvailableTokens(clientId),
                        60000
                );
            }

            // Input validation
            if (command == null || command.trim().isEmpty()) {
                throw new ValidationException("command", command, "Command cannot be empty");
            }

            if (serverName == null || serverName.trim().isEmpty()) {
                throw new ValidationException("serverName", serverName, "Server name cannot be empty");
            }

            // Security validation
            if (!commandValidator.isValid(command)) {
                auditLogger.logSecurityEvent("BLOCKED_COMMAND",
                        String.format("Command blocked: %s on %s", command, serverName));
                throw new ValidationException(
                        "command",
                        command,
                        "security-policy",
                        "Command failed security validation"
                );
            }

            // Sanitize command
            String sanitizedCommand = commandValidator.sanitize(command);

            // Audit logging
            auditLogger.logCommandExecution(serverName, sanitizedCommand, useSudo);

            // Execute with timeout
            CompletableFuture<CommandResult> future = CompletableFuture.supplyAsync(() ->
                    sshService.executeCommand(serverName, sanitizedCommand, useSudo)
            );

            CommandResult result = future.get(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Format and return result
            return responseFormatter.formatCommandResult(result);

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for client: {}", e.getClientId());
            return responseFormatter.formatError(
                    "Rate limit exceeded. Please wait before trying again.", e);

        } catch (ValidationException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return responseFormatter.formatError(e.getMessage(), e);

        } catch (SSHConnectionException e) {
            log.error("SSH connection failed: {}", e.getMessage());
            return responseFormatter.formatError(
                    "Failed to connect to server: " + e.getMessage(), e);

        } catch (CommandExecutionException e) {
            log.error("Command execution failed: {}", e.getMessage());
            return responseFormatter.formatError(
                    "Command execution failed: " + e.getMessage(), e);

        } catch (TimeoutException e) {
            log.error("Command execution timeout for: {} on {}", command, serverName);
            return responseFormatter.formatError(
                    "Command execution timeout exceeded", e);

        } catch (Exception e) {
            log.error("Unexpected error executing command", e);
            return responseFormatter.formatError(
                    "Unexpected error: " + e.getMessage(), e);
        }
    }

    @McpTool(name = "execute_script",
            description = "Execute a multi-line script with transaction support and rollback capability")
    public String executeScript(
            @McpToolParam(description = "Multi-line bash/shell script content") String script,
            @McpToolParam(description = "The target server identifier") String serverName,
            @McpToolParam(description = "Whether to execute with sudo privileges") boolean useSudo,
            @McpToolParam(description = "Stop on first error (true) or continue (false)") boolean stopOnError) {

        log.info("Executing script on server: {} (lines: {})",
                serverName, script.split("\n").length);

        try {
            // Rate limiting
            String clientId = getCurrentClientId();
            if (!rateLimiter.tryConsume(clientId, 3)) { // Scripts consume more tokens
                throw new RateLimitExceededException(clientId,
                        rateLimiter.getAvailableTokens(clientId), 60000);
            }

            // Validation
            if (script == null || script.trim().isEmpty()) {
                throw new ValidationException("script", script, "Script cannot be empty");
            }

            String[] commands = script.split("\n");
            StringBuilder output = new StringBuilder();
            StringBuilder errors = new StringBuilder();
            int successCount = 0;
            int failureCount = 0;

            for (int i = 0; i < commands.length; i++) {
                String cmd = commands[i].trim();
                if (cmd.isEmpty() || cmd.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                // Validate each command
                if (!commandValidator.isValid(cmd)) {
                    String error = String.format("Line %d: Command blocked by security policy: %s",
                            i + 1, cmd);
                    errors.append(error).append("\n");

                    if (stopOnError) {
                        throw new ValidationException("script", cmd,
                                "security-policy", error);
                    }
                    failureCount++;
                    continue;
                }

                try {
                    CommandResult result = sshService.executeCommand(
                            serverName,
                            commandValidator.sanitize(cmd),
                            useSudo
                    );

                    output.append(String.format("[Line %d: %s]\n", i + 1, cmd));
                    output.append(result.getOutput()).append("\n");

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                        errors.append(String.format("Line %d failed: %s\n",
                                i + 1, result.getError()));

                        if (stopOnError) {
                            return responseFormatter.formatError(
                                    String.format("Script failed at line %d: %s",
                                            i + 1, cmd),
                                    new CommandExecutionException(result.getError(), cmd,
                                            serverName, result.getExitCode())
                            );
                        }
                    }

                } catch (Exception e) {
                    failureCount++;
                    errors.append(String.format("Line %d error: %s\n",
                            i + 1, e.getMessage()));

                    if (stopOnError) {
                        throw e;
                    }
                }
            }

            // Build result summary
            return responseFormatter.formatSuccess(
                    String.format("Script execution completed. Success: %d, Failed: %d",
                            successCount, failureCount),
                    Map.of(
                            "output", output.toString(),
                            "errors", errors.toString(),
                            "successCount", successCount,
                            "failureCount", failureCount
                    )
            );

        } catch (Exception e) {
            log.error("Script execution failed", e);
            return responseFormatter.formatError(
                    "Script execution failed: " + e.getMessage(), e);
        }
    }

    @McpTool(name = "execute_parallel_commands",
            description = "Execute multiple commands in parallel across different servers")
    public String executeParallelCommands(
            @McpToolParam(description = "List of commands to execute") List<String> commands,
            @McpToolParam(description = "List of target servers") List<String> servers,
            @McpToolParam(description = "Maximum parallel executions") int maxParallel) {

        log.info("Executing {} commands on {} servers in parallel",
                commands.size(), servers.size());

        try {
            if (commands == null || commands.isEmpty()) {
                throw new ValidationException("commands", commands, "Commands list cannot be empty");
            }

            if (servers == null || servers.isEmpty()) {
                throw new ValidationException("servers", servers, "Servers list cannot be empty");
            }

            // Rate limiting for bulk operations
            String clientId = getCurrentClientId();
            int tokensNeeded = commands.size() * servers.size();
            if (!rateLimiter.tryConsume(clientId, Math.min(tokensNeeded, 10))) {
                throw new RateLimitExceededException(clientId,
                        rateLimiter.getAvailableTokens(clientId), 60000);
            }

            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

            for (String server : servers) {
                for (String command : commands) {
                    CompletableFuture<Map<String, Object>> future =
                            CompletableFuture.supplyAsync(() -> {
                                try {
                                    if (!commandValidator.isValid(command)) {
                                        return Map.of(
                                                "server", server,
                                                "command", command,
                                                "success", false,
                                                "error", "Command blocked by security policy"
                                        );
                                    }

                                    CommandResult result = sshService.executeCommand(
                                            server,
                                            commandValidator.sanitize(command),
                                            false
                                    );

                                    return Map.of(
                                            "server", server,
                                            "command", command,
                                            "success", result.isSuccess(),
                                            "output", result.isSuccess() ? result.getOutput() : result.getError()
                                    );
                                } catch (Exception e) {
                                    return Map.of(
                                            "server", server,
                                            "command", command,
                                            "success", false,
                                            "error", e.getMessage()
                                    );
                                }
                            });

                    futures.add(future);

                    // Limit parallel executions
                    if (futures.size() >= maxParallel) {
                        CompletableFuture.allOf(
                                futures.toArray(new CompletableFuture[0])
                        ).join();
                        futures.clear();
                    }
                }
            }

            // Wait for remaining futures
            CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            ).join();

            List<Map<String, Object>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            long successCount = results.stream()
                    .filter(r -> (boolean) r.get("success"))
                    .count();

            return responseFormatter.formatSuccess(
                    String.format("Parallel execution completed. Success: %d/%d",
                            successCount, results.size()),
                    results
            );

        } catch (Exception e) {
            log.error("Parallel execution failed", e);
            return responseFormatter.formatError(
                    "Parallel execution failed: " + e.getMessage(), e);
        }
    }

    private String getCurrentClientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }
}
package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.SSHService;
import net.alishahidi.mcpconductor.security.CommandValidator;
import net.alishahidi.mcpconductor.security.AuditLogger;
import net.alishahidi.mcpconductor.model.CommandResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandExecutionTool {

    private final SSHService sshService;
    private final CommandValidator commandValidator;
    private final AuditLogger auditLogger;

    @Tool(description = "Execute a shell command on the remote server")
    public CommandResult executeCommand(String command,
                                        String serverName,
                                        boolean useSudo) {
        log.info("Executing command: {} on server: {}", command, serverName);

        // Validate command
        if (!commandValidator.isValid(command)) {
            throw new IllegalArgumentException("Command validation failed: " + command);
        }

        // Audit log
        auditLogger.logCommandExecution(serverName, command, useSudo);

        try {
            return sshService.executeCommand(serverName, command, useSudo);
        } catch (Exception e) {
            log.error("Command execution failed", e);
            return CommandResult.failure(e.getMessage());
        }
    }

    @Tool(description = "Execute multiple commands in sequence on the remote server")
    public CommandResult executeScript(String script,
                                       String serverName,
                                       boolean useSudo) {
        log.info("Executing script on server: {}", serverName);

        String[] commands = script.split("\n");
        StringBuilder output = new StringBuilder();

        for (String command : commands) {
            if (command.trim().isEmpty()) continue;

            CommandResult result = executeCommand(command.trim(), serverName, useSudo);
            output.append(result.getOutput()).append("\n");

            if (!result.isSuccess()) {
                return CommandResult.failure("Script failed at: " + command);
            }
        }

        return CommandResult.success(output.toString());
    }
}
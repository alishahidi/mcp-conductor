package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.SSHService;
import net.alishahidi.mcpconductor.security.CommandValidator;
import net.alishahidi.mcpconductor.security.AuditLogger;
import net.alishahidi.mcpconductor.model.CommandResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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

    @Tool(name = "execute_command", description = "Execute a single command on a remote server via SSH. Use this to run any Linux/Unix command on connected servers. Perfect for system administration, file operations, process management, and DevOps tasks.")
    public CommandResult executeCommand(
            @ToolParam(description = "The Linux/Unix command to execute (e.g., 'ls -la', 'ps aux', 'systemctl status nginx', 'docker ps'). Avoid interactive commands.") String command,
            @ToolParam(description = "The target server identifier/name to execute the command on. Use 'localhost' for local execution or specific server names like 'production', 'staging' as configured.") String serverName,
            @ToolParam(description = "Whether to execute the command with sudo privileges (true/false). Use true for administrative commands that require root access.") boolean useSudo) {
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

    @Tool(name = "execute_script", description = "Execute a multi-line script (bash/shell script) on a remote server via SSH. Perfect for running complex automation scripts, deployment scripts, or multiple related commands sequentially.")
    public CommandResult executeScript(
            @ToolParam(description = "Multi-line bash/shell script content with commands separated by newlines. Each line will be executed sequentially. Example: 'cd /var/www\\nls -la\\nps aux | grep nginx'") String script,
            @ToolParam(description = "The target server identifier/name to execute the script on. Use 'localhost' for local execution or specific server names like 'production', 'staging' as configured.") String serverName,
            @ToolParam(description = "Whether to execute all script commands with sudo privileges (true/false). Use true when script contains administrative commands requiring root access.") boolean useSudo) {
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
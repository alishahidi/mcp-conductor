package net.alishahidi.mcpconductor.exception;

public class CommandExecutionException extends RuntimeException {

    private final String command;
    private final String serverName;
    private final int exitCode;

    public CommandExecutionException(String message) {
        super(message);
        this.command = null;
        this.serverName = null;
        this.exitCode = -1;
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.command = null;
        this.serverName = null;
        this.exitCode = -1;
    }

    public CommandExecutionException(String message, String command, String serverName, int exitCode) {
        super(message);
        this.command = command;
        this.serverName = serverName;
        this.exitCode = exitCode;
    }

    public String getCommand() {
        return command;
    }

    public String getServerName() {
        return serverName;
    }

    public int getExitCode() {
        return exitCode;
    }
}
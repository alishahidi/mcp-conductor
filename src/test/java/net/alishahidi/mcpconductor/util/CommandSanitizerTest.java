package net.alishahidi.mcpconductor.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandSanitizerTest {

    private CommandSanitizer commandSanitizer;

    @BeforeEach
    void setUp() {
        commandSanitizer = new CommandSanitizer();
    }

    @Test
    void testSafeCommand() {
        String command = "ls -la /home/user";
        assertTrue(commandSanitizer.isCommandSafe(command));
    }

    @Test
    void testDangerousCommand() {
        String command = "rm -rf /";
        assertFalse(commandSanitizer.isCommandSafe(command));
    }

    @Test
    void testCommandWithInjection() {
        String command = "ls; cat /etc/passwd";
        assertFalse(commandSanitizer.isCommandSafe(command));
    }

    @Test
    void testSanitizeCommand() {
        String command = "echo $(whoami)";
        String sanitized = commandSanitizer.sanitizeCommand(command);
        assertThat(sanitized).doesNotContain("$(");
    }

    @Test
    void testPathTraversal() {
        String path = "../../../etc/passwd";
        assertTrue(commandSanitizer.hasPathTraversal(path));
    }

    @Test
    void testSanitizePath() {
        String path = "../../../etc/passwd";
        String sanitized = commandSanitizer.sanitizePath(path);
        assertThat(sanitized).doesNotContain("../");
    }

    @Test
    void testEscapeShellArgument() {
        String arg = "test'argument";
        String escaped = commandSanitizer.escapeShellArgument(arg);
        assertThat(escaped).startsWith("'");
        assertThat(escaped).endsWith("'");
    }
}
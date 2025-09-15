package net.alishahidi.mcpconductor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandValidatorTest {

    private CommandValidator commandValidator;

    @BeforeEach
    void setUp() {
        // Create validator with strict mode and allowed commands
        List<String> allowedCommands = List.of("ls", "pwd", "cat", "grep", "ps", "top");
        commandValidator = new CommandValidator(true, allowedCommands);
    }

    @Test
    void testValidCommand() {
        assertTrue(commandValidator.isValid("ls -la"));
        assertTrue(commandValidator.isValid("pwd"));
        assertTrue(commandValidator.isValid("cat /var/log/test.log"));
    }

    @Test
    void testInvalidDangerousCommand() {
        assertFalse(commandValidator.isValid("rm -rf /"));
        assertFalse(commandValidator.isValid("dd if=/dev/zero of=/dev/sda"));
        assertFalse(commandValidator.isValid("mkfs.ext4 /dev/sda1"));
    }

    @Test
    void testCommandWithInjection() {
        assertFalse(commandValidator.isValid("ls $(whoami)"));
        assertFalse(commandValidator.isValid("cat `id`"));
        assertFalse(commandValidator.isValid("ls; rm file"));
        assertFalse(commandValidator.isValid("ls && rm file"));
        // Note: "ls | sh" might be valid as pipe to sh is not inherently blocked
        // but the "sh" command itself should not be in whitelist, making it invalid
    }

    @Test
    void testCommandNotInWhitelist() {
        // In strict mode, commands not in whitelist should be invalid
        assertFalse(commandValidator.isValid("wget http://example.com"));
        assertFalse(commandValidator.isValid("curl http://example.com"));
        assertFalse(commandValidator.isValid("nc -l 1234"));
    }

    @Test
    void testSanitizeCommand() {
        String sanitized = commandValidator.sanitize("ls $(whoami) && rm file");
        assertThat(sanitized).doesNotContain("$(");
        assertThat(sanitized).doesNotContain("&&");
    }

    @Test
    void testEmptyOrNullCommand() {
        assertFalse(commandValidator.isValid(null));
        assertFalse(commandValidator.isValid(""));
        assertFalse(commandValidator.isValid("   "));
    }

    @Test
    void testNonStrictMode() {
        // Create non-strict validator
        CommandValidator nonStrictValidator = new CommandValidator(false, List.of());
        
        // Should allow commands not in whitelist (since not in strict mode)
        // but still block dangerous ones
        assertFalse(nonStrictValidator.isValid("rm -rf /"));
        assertTrue(nonStrictValidator.isValid("echo hello"));
    }
}
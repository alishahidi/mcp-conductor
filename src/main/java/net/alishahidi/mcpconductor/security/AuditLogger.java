package net.alishahidi.mcpconductor.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogger {

    private final ObjectMapper objectMapper;

    @Value("${audit.log.file:audit.log}")
    private String auditLogFile;

    @Value("${audit.log.enabled:true}")
    private boolean auditEnabled;

    @Async
    public void logCommandExecution(String serverName, String command, boolean useSudo) {
        if (!auditEnabled) return;

        Map<String, Object> auditEntry = new HashMap<>();
        auditEntry.put("id", UUID.randomUUID().toString());
        auditEntry.put("timestamp", LocalDateTime.now());
        auditEntry.put("event", "COMMAND_EXECUTION");
        auditEntry.put("server", serverName);
        auditEntry.put("command", command);
        auditEntry.put("sudo", useSudo);
        auditEntry.put("user", getCurrentUser());

        writeAuditLog(auditEntry);
    }

    @Async
    public void logFileOperation(String serverName, String operation, String path) {
        if (!auditEnabled) return;

        Map<String, Object> auditEntry = new HashMap<>();
        auditEntry.put("id", UUID.randomUUID().toString());
        auditEntry.put("timestamp", LocalDateTime.now());
        auditEntry.put("event", "FILE_OPERATION");
        auditEntry.put("server", serverName);
        auditEntry.put("operation", operation);
        auditEntry.put("path", path);
        auditEntry.put("user", getCurrentUser());

        writeAuditLog(auditEntry);
    }

    @Async
    public void logSecurityEvent(String event, String details) {
        if (!auditEnabled) return;

        Map<String, Object> auditEntry = new HashMap<>();
        auditEntry.put("id", UUID.randomUUID().toString());
        auditEntry.put("timestamp", LocalDateTime.now());
        auditEntry.put("event", "SECURITY_" + event);
        auditEntry.put("details", details);
        auditEntry.put("user", getCurrentUser());

        writeAuditLog(auditEntry);
    }

    private void writeAuditLog(Map<String, Object> entry) {
        try (FileWriter writer = new FileWriter(auditLogFile, true)) {
            String json = objectMapper.writeValueAsString(entry);
            writer.write(json + "\n");
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to write audit log", e);
        }
    }

    private String getCurrentUser() {
        // In a real implementation, this would get the authenticated user
        return System.getProperty("user.name", "unknown");
    }
}
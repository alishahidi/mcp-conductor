package net.alishahidi.mcpconductor.util;

import net.alishahidi.mcpconductor.model.CommandResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseFormatter {
    
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public String formatCommandResult(CommandResult result) {
        if (result == null) {
            return formatError("Invalid command result");
        }
        
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", result.isSuccess());
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            
            if (result.isSuccess()) {
                response.put("output", result.getOutput());
                response.put("exitCode", result.getExitCode());
            } else {
                response.put("error", result.getError());
                response.put("exitCode", result.getExitCode());
            }
            
            if (result.getExecutedAt() != null) {
                response.put("executedAt", result.getExecutedAt().format(DATE_FORMATTER));
            }
            
            if (result.getExecutionTimeMs() > 0) {
                response.put("executionTimeMs", result.getExecutionTimeMs());
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format command result", e);
            return formatError("Failed to format response: " + e.getMessage());
        }
    }
    
    public String formatSuccess(String message) {
        return formatSuccess(message, null);
    }
    
    public String formatSuccess(String message, Object data) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("message", message);
            
            if (data != null) {
                response.set("data", objectMapper.valueToTree(data));
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format success response", e);
            return "{\"success\":false,\"error\":\"Failed to format response\"}";
        }
    }
    
    public String formatError(String errorMessage) {
        return formatError(errorMessage, null);
    }
    
    public String formatError(String errorMessage, Exception exception) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", false);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("error", errorMessage);
            
            if (exception != null) {
                response.put("exception", exception.getClass().getSimpleName());
                response.put("exceptionMessage", exception.getMessage());
                
                // Add stack trace in debug mode
                if (log.isDebugEnabled()) {
                    ArrayNode stackTrace = objectMapper.createArrayNode();
                    for (StackTraceElement element : exception.getStackTrace()) {
                        stackTrace.add(element.toString());
                    }
                    response.set("stackTrace", stackTrace);
                }
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format error response", e);
            return "{\"success\":false,\"error\":\"Failed to format error response\"}";
        }
    }
    
    public String formatList(List<?> items) {
        return formatList(items, null);
    }
    
    public String formatList(List<?> items, String description) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("count", items.size());
            
            if (description != null) {
                response.put("description", description);
            }
            
            response.set("items", objectMapper.valueToTree(items));
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format list response", e);
            return formatError("Failed to format list: " + e.getMessage());
        }
    }
    
    public String formatMap(Map<String, ?> data) {
        return formatMap(data, null);
    }
    
    public String formatMap(Map<String, ?> data, String description) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            
            if (description != null) {
                response.put("description", description);
            }
            
            response.set("data", objectMapper.valueToTree(data));
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format map response", e);
            return formatError("Failed to format map: " + e.getMessage());
        }
    }
    
    public String formatProgress(String operation, int completed, int total) {
        return formatProgress(operation, completed, total, null);
    }
    
    public String formatProgress(String operation, int completed, int total, String currentItem) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("operation", operation);
            response.put("completed", completed);
            response.put("total", total);
            response.put("percentage", total > 0 ? (completed * 100.0 / total) : 0);
            
            if (currentItem != null) {
                response.put("currentItem", currentItem);
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format progress response", e);
            return formatError("Failed to format progress: " + e.getMessage());
        }
    }
    
    public String formatSystemInfo(Map<String, Object> systemInfo) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("type", "system_info");
            response.set("system", objectMapper.valueToTree(systemInfo));
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format system info response", e);
            return formatError("Failed to format system info: " + e.getMessage());
        }
    }
    
    public String formatFileOperation(String operation, String path, boolean success) {
        return formatFileOperation(operation, path, success, null);
    }
    
    public String formatFileOperation(String operation, String path, boolean success, String details) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", success);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("operation", operation);
            response.put("path", path);
            
            if (details != null) {
                response.put("details", details);
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format file operation response", e);
            return formatError("Failed to format file operation response: " + e.getMessage());
        }
    }
    
    public String formatServiceStatus(String serviceName, String status, Map<String, Object> details) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("service", serviceName);
            response.put("status", status);
            
            if (details != null && !details.isEmpty()) {
                response.set("details", objectMapper.valueToTree(details));
            }
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format service status response", e);
            return formatError("Failed to format service status: " + e.getMessage());
        }
    }
    
    public String formatDockerInfo(String operation, Object dockerData) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("operation", operation);
            response.set("docker", objectMapper.valueToTree(dockerData));
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format docker info response", e);
            return formatError("Failed to format docker info: " + e.getMessage());
        }
    }
    
    public String formatGitOperation(String operation, String repository, boolean success, String output) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", success);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("operation", operation);
            response.put("repository", repository);
            response.put("output", output);
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format git operation response", e);
            return formatError("Failed to format git operation response: " + e.getMessage());
        }
    }
    
    public String formatPlainText(String text) {
        if (text == null) {
            return "";
        }
        
        // Simple formatting for plain text responses
        return text.trim()
                  .replaceAll("\\n{3,}", "\n\n")  // Reduce multiple newlines
                  .replaceAll("\\s+$", "");       // Remove trailing whitespace
    }
    
    public String formatTable(List<Map<String, Object>> rows, List<String> columns) {
        if (rows.isEmpty()) {
            return formatSuccess("No data to display");
        }
        
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            response.put("type", "table");
            response.set("columns", objectMapper.valueToTree(columns));
            response.set("rows", objectMapper.valueToTree(rows));
            response.put("rowCount", rows.size());
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to format table response", e);
            return formatError("Failed to format table: " + e.getMessage());
        }
    }
}
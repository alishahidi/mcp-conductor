package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperation {

    public enum OperationType {
        READ, WRITE, APPEND, DELETE, COPY, MOVE, CHMOD, CHOWN, CREATE_DIR, LIST
    }

    private OperationType type;
    private String path;
    private String content;
    private String permissions;
    private String owner;
    private String group;
    private LocalDateTime timestamp;
    private boolean successful;
    private String errorMessage;
}
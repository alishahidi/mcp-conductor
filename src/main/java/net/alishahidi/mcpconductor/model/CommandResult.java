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
public class CommandResult {
    private boolean success;
    private String output;
    private String error;
    private int exitCode;
    private LocalDateTime executedAt;
    private long executionTimeMs;

    public static CommandResult success(String output) {
        return CommandResult.builder()
                .success(true)
                .output(output)
                .exitCode(0)
                .executedAt(LocalDateTime.now())
                .build();
    }

    public static CommandResult failure(String error) {
        return CommandResult.builder()
                .success(false)
                .error(error)
                .exitCode(1)
                .executedAt(LocalDateTime.now())
                .build();
    }
}
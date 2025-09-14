package net.alishahidi.mcpconductor.config;

import org.springframework.ai.mcp.server.McpServerFeatures;
import org.springframework.ai.mcp.server.McpSyncServerExchange;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.BiConsumer;

@Configuration
@Slf4j
public class McpServerConfig {

    @Value("${spring.ai.mcp.server.name:mcp-devops-commander}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String serverVersion;

    @Bean
    public ToolCallbackProvider devOpsToolProvider(
            CommandExecutionTool commandTool,
            DockerManagementTool dockerTool,
            PackageManagementTool packageTool,
            FileOperationsTool fileTool,
            GitOperationsTool gitTool,
            ServiceManagementTool serviceTool,
            NginxConfigurationTool nginxTool,
            SystemMonitoringTool systemTool) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        commandTool,
                        dockerTool,
                        packageTool,
                        fileTool,
                        gitTool,
                        serviceTool,
                        nginxTool,
                        systemTool
                )
                .build();
    }

    @Bean
    public BiConsumer<McpSyncServerExchange, List<McpServerFeatures.Root>> rootsChangeHandler() {
        return (exchange, roots) -> {
            log.info("MCP Server roots registered: {}", roots);
            roots.forEach(root ->
                    log.debug("Root: {} - {}", root.uri(), root.name())
            );
        };
    }

    @Bean
    public List<McpServerFeatures.SyncCompletionSpecification> completionSpecs() {
        return List.of(
                new McpServerFeatures.SyncCompletionSpecification(
                        "command-completion",
                        "Provides command completion suggestions",
                        (exchange, request) -> {
                            // Command completion logic
                            return new McpServerFeatures.CompletionResult(
                                    List.of(
                                            new McpServerFeatures.Completion("apt install", "Install package with apt"),
                                            new McpServerFeatures.Completion("docker ps", "List Docker containers"),
                                            new McpServerFeatures.Completion("systemctl status", "Check service status")
                                    )
                            );
                        }
                )
        );
    }
}
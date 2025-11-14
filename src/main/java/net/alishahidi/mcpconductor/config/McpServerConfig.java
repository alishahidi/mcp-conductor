package net.alishahidi.mcpconductor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * MCP Server Configuration
 *
 * Spring AI MCP Server is auto-configured through spring-ai-starter-mcp-server.
 * This class provides logging and initialization for the MCP server.
 *
 * Key configuration properties (application.yml):
 * - spring.ai.mcp.server.stdio: true (for STDIO mode)
 * - spring.ai.mcp.server.type: SYNC (for synchronous operations)
 * - spring.ai.mcp.server.annotation-scanner.enabled: true (to scan for @McpTool annotations)
 */
@Configuration
@Slf4j
public class McpServerConfig {

    @Value("${spring.ai.mcp.server.name:mcp-conductor}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String serverVersion;

    @Value("${spring.ai.mcp.server.stdio:false}")
    private boolean stdioEnabled;

    @PostConstruct
    public void init() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  MCP Conductor Server Initialized                           ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Server Name:    {}                              ║", serverName);
        log.info("║  Version:        {}                                      ║", serverVersion);
        log.info("║  Mode:           {}                                     ║", stdioEnabled ? "STDIO" : "HTTP");
        log.info("║  Type:           SYNC                                        ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }
}
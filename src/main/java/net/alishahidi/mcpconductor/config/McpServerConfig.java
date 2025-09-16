package net.alishahidi.mcpconductor.config;

import net.alishahidi.mcpconductor.tools.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class McpServerConfig {

    @Value("${spring.ai.mcp.server.name:mcp-conductor}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String serverVersion;

    @Bean
    @ConfigurationProperties(prefix = "spring.ai.mcp.server")
    @ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "none")
    public String mcpServerConfiguration() {
        log.info("Configuring MCP STDIO Server: {}", serverName);
        log.info("Server Version: {}", serverVersion);
        log.info("Running in STDIO mode for Claude Code integration");
        return "STDIO mode enabled";
    }

    @Bean
    public String mcpServerInfo() {
        log.info("MCP Conductor Server initialized");
        log.info("Server Name: {}", serverName);
        log.info("Server Version: {}", serverVersion);
        return "MCP Conductor Server v" + serverVersion;
    }
}
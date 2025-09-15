package net.alishahidi.mcpconductor.config;

import net.alishahidi.mcpconductor.tools.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class McpServerConfig {

    @Value("${spring.ai.mcp.server.name:mcp-devops-commander}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String serverVersion;

    @Bean
    public String mcpServerInfo() {
        log.info("MCP DevOps Commander Server initialized");
        log.info("Server Name: {}", serverName);
        log.info("Server Version: {}", serverVersion);
        return "MCP DevOps Commander Server v" + serverVersion;
    }
}
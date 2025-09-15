package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.NginxService;
import net.alishahidi.mcpconductor.model.NginxSite;
import net.alishahidi.mcpconductor.util.ResponseFormatter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NginxConfigurationTool {
    
    private final NginxService nginxService;
    private final ResponseFormatter responseFormatter;

    @Tool(name = "create_nginx_site", description = "Create a new Nginx site configuration for web hosting or reverse proxy. Perfect for setting up websites, APIs, or load balancing.")
    public String createNginxSite(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The domain name or site name (e.g., 'example.com', 'api.mysite.com')") String siteName,
            @ToolParam(description = "Port to listen on (default: 80 for HTTP, 443 for HTTPS)") int port,
            @ToolParam(description = "Document root path for static files (e.g., '/var/www/html') - optional for reverse proxy") String documentRoot,
            @ToolParam(description = "Proxy pass URL for reverse proxy (e.g., 'http://localhost:3000') - optional for static sites") String proxyPass,
            @ToolParam(description = "Whether to enable SSL/HTTPS (true/false)") boolean enableSsl) {
        log.info("Creating Nginx site: {} on server: {}", siteName, serverName);
        
        try {
            NginxSite.NginxSiteBuilder siteBuilder = NginxSite.builder()
                    .serverName(siteName)
                    .port(port)
                    .documentRoot(documentRoot)
                    .proxyPass(proxyPass)
                    .sslEnabled(enableSsl);
            
            if (enableSsl) {
                siteBuilder.sslPort(443);
            }
            
            NginxSite site = siteBuilder.build();
            
            nginxService.createSiteConfiguration(serverName, site);
            return responseFormatter.formatSuccess("Nginx site configuration created successfully for: " + siteName);
            
        } catch (Exception e) {
            log.error("Failed to create Nginx site: {}", siteName, e);
            return responseFormatter.formatError("Failed to create Nginx site: " + e.getMessage());
        }
    }

    @Tool(name = "enable_nginx_site", description = "Enable an existing Nginx site configuration by creating a symbolic link. The site will start serving traffic after reload.")
    public String enableNginxSite(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The site name to enable (must exist in sites-available)") String siteName) {
        log.info("Enabling Nginx site: {} on server: {}", siteName, serverName);
        
        try {
            nginxService.enableSite(serverName, siteName);
            return responseFormatter.formatSuccess("Nginx site enabled successfully: " + siteName);
        } catch (Exception e) {
            log.error("Failed to enable Nginx site: {}", siteName, e);
            return responseFormatter.formatError("Failed to enable Nginx site: " + e.getMessage());
        }
    }

    @Tool(name = "disable_nginx_site", description = "Disable an Nginx site configuration by removing the symbolic link. The site will stop serving traffic after reload.")
    public String disableNginxSite(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The site name to disable") String siteName) {
        log.info("Disabling Nginx site: {} on server: {}", siteName, serverName);
        
        try {
            nginxService.disableSite(serverName, siteName);
            return responseFormatter.formatSuccess("Nginx site disabled successfully: " + siteName);
        } catch (Exception e) {
            log.error("Failed to disable Nginx site: {}", siteName, e);
            return responseFormatter.formatError("Failed to disable Nginx site: " + e.getMessage());
        }
    }

    @Tool(name = "delete_nginx_site", description = "Completely remove an Nginx site configuration. This will disable the site and delete the configuration file.")
    public String deleteNginxSite(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The site name to delete") String siteName) {
        log.info("Deleting Nginx site: {} on server: {}", siteName, serverName);
        
        try {
            nginxService.deleteSite(serverName, siteName);
            return responseFormatter.formatSuccess("Nginx site deleted successfully: " + siteName);
        } catch (Exception e) {
            log.error("Failed to delete Nginx site: {}", siteName, e);
            return responseFormatter.formatError("Failed to delete Nginx site: " + e.getMessage());
        }
    }

    @Tool(name = "reload_nginx", description = "Reload Nginx configuration to apply changes. This is required after enabling/disabling sites or making configuration changes.")
    public String reloadNginx(
            @ToolParam(description = "The target server name where Nginx is running") String serverName) {
        log.info("Reloading Nginx configuration on server: {}", serverName);
        
        try {
            nginxService.reloadConfiguration(serverName);
            return responseFormatter.formatSuccess("Nginx configuration reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload Nginx configuration", e);
            return responseFormatter.formatError("Failed to reload Nginx: " + e.getMessage());
        }
    }

    @Tool(name = "test_nginx_config", description = "Test Nginx configuration syntax for errors before reloading. Always run this before applying changes to production.")
    public String testNginxConfig(
            @ToolParam(description = "The target server name where Nginx is running") String serverName) {
        log.info("Testing Nginx configuration on server: {}", serverName);
        
        try {
            boolean isValid = nginxService.testConfiguration(serverName);
            if (isValid) {
                return responseFormatter.formatSuccess("Nginx configuration syntax is valid");
            } else {
                return responseFormatter.formatError("Nginx configuration has syntax errors");
            }
        } catch (Exception e) {
            log.error("Failed to test Nginx configuration", e);
            return responseFormatter.formatError("Failed to test Nginx configuration: " + e.getMessage());
        }
    }

    @Tool(name = "list_nginx_sites", description = "List all available and enabled Nginx sites. Shows both available configurations and currently active sites.")
    public String listNginxSites(
            @ToolParam(description = "The target server name where Nginx is running") String serverName) {
        log.info("Listing Nginx sites on server: {}", serverName);
        
        try {
            List<String> availableSites = nginxService.listAvailableSites(serverName);
            List<String> enabledSites = nginxService.listEnabledSites(serverName);
            
            StringBuilder result = new StringBuilder();
            result.append("Available sites: ").append(String.join(", ", availableSites)).append("\n");
            result.append("Enabled sites: ").append(String.join(", ", enabledSites));
            
            return responseFormatter.formatSuccess("Nginx sites listed successfully", result.toString());
        } catch (Exception e) {
            log.error("Failed to list Nginx sites", e);
            return responseFormatter.formatError("Failed to list Nginx sites: " + e.getMessage());
        }
    }

    @Tool(name = "get_nginx_site_config", description = "Get the configuration content for a specific Nginx site. Useful for reviewing or debugging site configurations.")
    public String getNginxSiteConfig(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The site name to get configuration for") String siteName) {
        log.info("Getting Nginx site configuration for: {} on server: {}", siteName, serverName);
        
        try {
            String config = nginxService.getSiteConfiguration(serverName, siteName);
            return responseFormatter.formatSuccess("Site configuration retrieved successfully", config);
        } catch (Exception e) {
            log.error("Failed to get Nginx site configuration: {}", siteName, e);
            return responseFormatter.formatError("Failed to get site configuration: " + e.getMessage());
        }
    }

    @Tool(name = "create_reverse_proxy", description = "Create an Nginx reverse proxy configuration for load balancing or API gateway. Perfect for microservices architecture.")
    public String createReverseProxy(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The domain name for the proxy (e.g., 'api.example.com')") String domainName,
            @ToolParam(description = "The backend URL to proxy to (e.g., 'http://localhost:3000')") String backendUrl,
            @ToolParam(description = "Port to listen on (default: 80)") int port) {
        log.info("Creating reverse proxy: {} -> {} on server: {}", domainName, backendUrl, serverName);
        
        try {
            NginxSite site = NginxSite.builder()
                    .serverName(domainName)
                    .port(port)
                    .proxyPass(backendUrl)
                    .sslEnabled(false)
                    .build();
            
            nginxService.createSiteConfiguration(serverName, site);
            nginxService.enableSite(serverName, domainName);
            
            return responseFormatter.formatSuccess("Reverse proxy created and enabled successfully for: " + domainName);
        } catch (Exception e) {
            log.error("Failed to create reverse proxy: {}", domainName, e);
            return responseFormatter.formatError("Failed to create reverse proxy: " + e.getMessage());
        }
    }

    @Tool(name = "create_ssl_site", description = "Create an Nginx site with SSL/HTTPS configuration. Requires SSL certificate files to be already present on the server.")
    public String createSslSite(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "The domain name (e.g., 'secure.example.com')") String domainName,
            @ToolParam(description = "Path to SSL certificate file (e.g., '/etc/ssl/certs/example.com.crt')") String certPath,
            @ToolParam(description = "Path to SSL private key file (e.g., '/etc/ssl/private/example.com.key')") String keyPath,
            @ToolParam(description = "Document root for static files or null for reverse proxy") String documentRoot,
            @ToolParam(description = "Backend URL for reverse proxy or null for static site") String proxyPass) {
        log.info("Creating SSL site: {} on server: {}", domainName, serverName);
        
        try {
            NginxSite site = NginxSite.builder()
                    .serverName(domainName)
                    .port(80)
                    .sslEnabled(true)
                    .sslPort(443)
                    .sslCertificate(certPath)
                    .sslCertificateKey(keyPath)
                    .documentRoot(documentRoot)
                    .proxyPass(proxyPass)
                    .build();
            
            nginxService.createSiteConfiguration(serverName, site);
            
            return responseFormatter.formatSuccess("SSL site configuration created successfully for: " + domainName);
        } catch (Exception e) {
            log.error("Failed to create SSL site: {}", domainName, e);
            return responseFormatter.formatError("Failed to create SSL site: " + e.getMessage());
        }
    }

    @Tool(name = "backup_nginx_config", description = "Create a backup of the entire Nginx configuration directory. Essential before making major changes.")
    public String backupNginxConfig(
            @ToolParam(description = "The target server name where Nginx is running") String serverName,
            @ToolParam(description = "Path where to save the backup file (e.g., '/tmp/nginx-backup.tar.gz')") String backupPath) {
        log.info("Backing up Nginx configuration on server: {} to: {}", serverName, backupPath);
        
        try {
            nginxService.backupConfiguration(serverName, backupPath);
            return responseFormatter.formatSuccess("Nginx configuration backed up successfully to: " + backupPath);
        } catch (Exception e) {
            log.error("Failed to backup Nginx configuration", e);
            return responseFormatter.formatError("Failed to backup Nginx configuration: " + e.getMessage());
        }
    }
}
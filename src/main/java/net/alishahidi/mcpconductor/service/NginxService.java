package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.model.NginxSite;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class NginxService {
    
    private final SSHService sshService;
    private final FileService fileService;
    
    public void createSiteConfiguration(String serverName, NginxSite site) {
        log.info("Creating Nginx site configuration for: {}", site.getServerName());
        
        String config = generateSiteConfig(site);
        String configPath = "/etc/nginx/sites-available/" + site.getServerName();
        
        try {
            // Write configuration file
            fileService.writeFile(serverName, configPath, config);
            log.info("Nginx site configuration created at: {}", configPath);
        } catch (Exception e) {
            log.error("Failed to create Nginx site configuration", e);
            throw new RuntimeException("Failed to create site configuration: " + e.getMessage());
        }
    }
    
    public void enableSite(String serverName, String siteName) {
        log.info("Enabling Nginx site: {}", siteName);
        
        try {
            String availablePath = "/etc/nginx/sites-available/" + siteName;
            String enabledPath = "/etc/nginx/sites-enabled/" + siteName;
            
            // Create symbolic link
            CommandResult result = sshService.executeCommand(serverName, 
                String.format("ln -sf %s %s", availablePath, enabledPath), true);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to enable site: " + result.getError());
            }
            
            log.info("Nginx site enabled: {}", siteName);
        } catch (Exception e) {
            log.error("Failed to enable Nginx site: {}", siteName, e);
            throw new RuntimeException("Failed to enable site: " + e.getMessage());
        }
    }
    
    public void disableSite(String serverName, String siteName) {
        log.info("Disabling Nginx site: {}", siteName);
        
        try {
            String enabledPath = "/etc/nginx/sites-enabled/" + siteName;
            
            // Remove symbolic link
            CommandResult result = sshService.executeCommand(serverName, 
                String.format("rm -f %s", enabledPath), true);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to disable site: " + result.getError());
            }
            
            log.info("Nginx site disabled: {}", siteName);
        } catch (Exception e) {
            log.error("Failed to disable Nginx site: {}", siteName, e);
            throw new RuntimeException("Failed to disable site: " + e.getMessage());
        }
    }
    
    public void deleteSite(String serverName, String siteName) {
        log.info("Deleting Nginx site: {}", siteName);
        
        try {
            // First disable the site
            disableSite(serverName, siteName);
            
            // Then remove the configuration file
            String availablePath = "/etc/nginx/sites-available/" + siteName;
            CommandResult result = sshService.executeCommand(serverName, 
                String.format("rm -f %s", availablePath), true);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to delete site configuration: " + result.getError());
            }
            
            log.info("Nginx site deleted: {}", siteName);
        } catch (Exception e) {
            log.error("Failed to delete Nginx site: {}", siteName, e);
            throw new RuntimeException("Failed to delete site: " + e.getMessage());
        }
    }
    
    public void reloadConfiguration(String serverName) {
        log.info("Reloading Nginx configuration");
        
        try {
            CommandResult result = sshService.executeCommand(serverName, 
                "nginx -s reload", true);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to reload Nginx: " + result.getError());
            }
            
            log.info("Nginx configuration reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload Nginx configuration", e);
            throw new RuntimeException("Failed to reload Nginx: " + e.getMessage());
        }
    }
    
    public boolean testConfiguration(String serverName) {
        log.info("Testing Nginx configuration");
        
        try {
            CommandResult result = sshService.executeCommand(serverName, 
                "nginx -t", false);
            
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Failed to test Nginx configuration", e);
            return false;
        }
    }
    
    public List<String> listAvailableSites(String serverName) {
        log.info("Listing available Nginx sites");
        
        try {
            CommandResult result = sshService.executeCommand(serverName, 
                "ls -1 /etc/nginx/sites-available/", false);
            
            if (result.isSuccess()) {
                List<String> sites = new ArrayList<>();
                String[] lines = result.getOutput().split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equals("default")) {
                        sites.add(line);
                    }
                }
                return sites;
            } else {
                throw new RuntimeException("Failed to list available sites: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to list available Nginx sites", e);
            throw new RuntimeException("Failed to list sites: " + e.getMessage());
        }
    }
    
    public List<String> listEnabledSites(String serverName) {
        log.info("Listing enabled Nginx sites");
        
        try {
            CommandResult result = sshService.executeCommand(serverName, 
                "ls -1 /etc/nginx/sites-enabled/", false);
            
            if (result.isSuccess()) {
                List<String> sites = new ArrayList<>();
                String[] lines = result.getOutput().split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equals("default")) {
                        sites.add(line);
                    }
                }
                return sites;
            } else {
                throw new RuntimeException("Failed to list enabled sites: " + result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to list enabled Nginx sites", e);
            throw new RuntimeException("Failed to list enabled sites: " + e.getMessage());
        }
    }
    
    public String getSiteConfiguration(String serverName, String siteName) {
        log.info("Getting Nginx site configuration for: {}", siteName);
        
        try {
            String configPath = "/etc/nginx/sites-available/" + siteName;
            return fileService.readFile(serverName, configPath);
        } catch (Exception e) {
            log.error("Failed to get site configuration for: {}", siteName, e);
            throw new RuntimeException("Failed to get site configuration: " + e.getMessage());
        }
    }
    
    public void backupConfiguration(String serverName, String backupPath) {
        log.info("Backing up Nginx configuration to: {}", backupPath);
        
        try {
            CommandResult result = sshService.executeCommand(serverName, 
                String.format("tar -czf %s -C /etc nginx/", backupPath), true);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to backup configuration: " + result.getError());
            }
            
            log.info("Nginx configuration backed up successfully");
        } catch (Exception e) {
            log.error("Failed to backup Nginx configuration", e);
            throw new RuntimeException("Failed to backup configuration: " + e.getMessage());
        }
    }
    
    private String generateSiteConfig(NginxSite site) {
        StringBuilder config = new StringBuilder();
        
        config.append("server {\n");
        config.append("    listen ").append(site.getPort()).append(";\n");
        
        if (site.isSslEnabled()) {
            config.append("    listen ").append(site.getSslPort()).append(" ssl;\n");
        }
        
        config.append("    server_name ").append(site.getServerName()).append(";\n");
        
        if (site.getDocumentRoot() != null) {
            config.append("    root ").append(site.getDocumentRoot()).append(";\n");
            config.append("    index index.html index.htm index.php;\n");
        }
        
        if (site.getProxyPass() != null) {
            config.append("\n    location / {\n");
            config.append("        proxy_pass ").append(site.getProxyPass()).append(";\n");
            config.append("        proxy_set_header Host $host;\n");
            config.append("        proxy_set_header X-Real-IP $remote_addr;\n");
            config.append("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
            config.append("        proxy_set_header X-Forwarded-Proto $scheme;\n");
            config.append("    }\n");
        } else {
            config.append("\n    location / {\n");
            config.append("        try_files $uri $uri/ =404;\n");
            config.append("    }\n");
        }
        
        if (site.isSslEnabled() && site.getSslCertificate() != null && site.getSslCertificateKey() != null) {
            config.append("\n    ssl_certificate ").append(site.getSslCertificate()).append(";\n");
            config.append("    ssl_certificate_key ").append(site.getSslCertificateKey()).append(";\n");
            config.append("    ssl_protocols TLSv1.2 TLSv1.3;\n");
            config.append("    ssl_ciphers HIGH:!aNULL:!MD5;\n");
        }
        
        if (site.getAccessLog() != null) {
            config.append("\n    access_log ").append(site.getAccessLog()).append(";\n");
        }
        
        if (site.getErrorLog() != null) {
            config.append("    error_log ").append(site.getErrorLog()).append(";\n");
        }
        
        config.append("}\n");
        
        return config.toString();
    }
}
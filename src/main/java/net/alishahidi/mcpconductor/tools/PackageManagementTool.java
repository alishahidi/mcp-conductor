package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.PackageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageManagementTool {

    private final PackageService packageService;

    @Tool(description = "Install a package using the system package manager")
    public String installPackage(String packageName,
                                 String serverName,
                                 String packageManager) {
        log.info("Installing package: {} on server: {} using: {}",
                packageName, serverName, packageManager);

        return packageService.installPackage(serverName, packageName, packageManager);
    }

    @Tool(description = "Uninstall a package from the system")
    public String uninstallPackage(String packageName,
                                   String serverName,
                                   String packageManager) {
        log.info("Uninstalling package: {} from server: {}", packageName, serverName);
        return packageService.uninstallPackage(serverName, packageName, packageManager);
    }

    @Tool(description = "Update system packages")
    public String updatePackages(String serverName, String packageManager) {
        log.info("Updating packages on server: {}", serverName);
        return packageService.updatePackages(serverName, packageManager);
    }

    @Tool(description = "Search for available packages")
    public List<String> searchPackages(String query,
                                       String serverName,
                                       String packageManager) {
        log.info("Searching packages with query: {} on server: {}", query, serverName);
        return packageService.searchPackages(serverName, query, packageManager);
    }

    @Tool(description = "Check if a package is installed")
    public boolean isPackageInstalled(String packageName,
                                      String serverName,
                                      String packageManager) {
        return packageService.isPackageInstalled(serverName, packageName, packageManager);
    }
}
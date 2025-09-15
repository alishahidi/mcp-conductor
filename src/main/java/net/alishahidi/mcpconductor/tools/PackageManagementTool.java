package net.alishahidi.mcpconductor.tools;

import net.alishahidi.mcpconductor.service.PackageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PackageManagementTool {

    private final PackageService packageService;

    @Tool(name = "package_install", description = "Install a package on a remote server using the specified package manager. Essential for setting up software dependencies, tools, and applications. Supports multiple Linux package managers for different distributions.")
    public String installPackage(
            @ToolParam(description = "The name of the package to install (e.g., 'nginx', 'docker.io', 'python3-pip', 'git', 'nodejs'). Use exact package names as they appear in the repository.") String packageName,
            @ToolParam(description = "The target server identifier where the package should be installed (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName,
            @ToolParam(description = "The package manager to use (e.g., 'apt' for Ubuntu/Debian, 'yum' for RHEL/CentOS, 'dnf' for Fedora, 'zypper' for openSUSE, 'pacman' for Arch). Must match the server's Linux distribution.") String packageManager) {
        log.info("Installing package: {} on server: {} using: {}",
                packageName, serverName, packageManager);

        return packageService.installPackage(serverName, packageName, packageManager);
    }

    @Tool(name = "package_uninstall", description = "Uninstall a package from a remote server using the specified package manager. Use for removing unused software, cleaning up dependencies, or uninstalling problematic packages. Helps maintain clean system state.")
    public String uninstallPackage(
            @ToolParam(description = "The name of the package to uninstall (e.g., 'nginx', 'old-kernel-version', 'unused-dev-tools'). Must be an installed package name.") String packageName,
            @ToolParam(description = "The target server identifier where the package should be uninstalled (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName,
            @ToolParam(description = "The package manager to use (e.g., 'apt' for Ubuntu/Debian, 'yum' for RHEL/CentOS, 'dnf' for Fedora, 'zypper' for openSUSE, 'pacman' for Arch). Must match the server's Linux distribution.") String packageManager) {
        log.info("Uninstalling package: {} from server: {}", packageName, serverName);
        return packageService.uninstallPackage(serverName, packageName, packageManager);
    }

    @Tool(name = "package_update", description = "Update all packages on a remote server using the specified package manager. Critical for security updates, bug fixes, and getting latest software versions. Essential for maintaining secure and up-to-date systems.")
    public String updatePackages(
            @ToolParam(description = "The target server identifier where packages should be updated (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName,
            @ToolParam(description = "The package manager to use for updates (e.g., 'apt' for Ubuntu/Debian, 'yum' for RHEL/CentOS, 'dnf' for Fedora, 'zypper' for openSUSE, 'pacman' for Arch). Must match the server's Linux distribution.") String packageManager) {
        log.info("Updating packages on server: {}", serverName);
        return packageService.updatePackages(serverName, packageManager);
    }

    @Tool(name = "package_search", description = "Search for packages on a remote server using the specified package manager. Perfect for finding available software, discovering package names, or exploring what's available in repositories before installation.")
    public List<String> searchPackages(
            @ToolParam(description = "The search term or pattern to look for (e.g., 'python', 'web server', 'database', 'docker'). Can be partial names, keywords, or descriptions to find relevant packages.") String query,
            @ToolParam(description = "The target server identifier where the search should be performed (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName,
            @ToolParam(description = "The package manager to use for searching (e.g., 'apt' for Ubuntu/Debian, 'yum' for RHEL/CentOS, 'dnf' for Fedora, 'zypper' for openSUSE, 'pacman' for Arch). Must match the server's Linux distribution.") String packageManager) {
        log.info("Searching packages with query: {} on server: {}", query, serverName);
        return packageService.searchPackages(serverName, query, packageManager);
    }

    @Tool(name = "package_is_installed", description = "Check if a package is installed on a remote server. Useful for verifying installations, checking dependencies, or determining if software needs to be installed before proceeding with other operations.")
    public boolean isPackageInstalled(
            @ToolParam(description = "The name of the package to check (e.g., 'nginx', 'docker.io', 'python3', 'git'). Must be exact package name as it appears in the package manager.") String packageName,
            @ToolParam(description = "The target server identifier where the check should be performed (e.g., 'production', 'staging', 'localhost'). Must be a configured server connection.") String serverName,
            @ToolParam(description = "The package manager to use for checking (e.g., 'apt' for Ubuntu/Debian, 'yum' for RHEL/CentOS, 'dnf' for Fedora, 'zypper' for openSUSE, 'pacman' for Arch). Must match the server's Linux distribution.") String packageManager) {
        return packageService.isPackageInstalled(serverName, packageName, packageManager);
    }
}
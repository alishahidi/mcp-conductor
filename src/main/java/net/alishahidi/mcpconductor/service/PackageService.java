package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.exception.CommandExecutionException;
import net.alishahidi.mcpconductor.exception.ValidationException;
import net.alishahidi.mcpconductor.model.CommandResult;
import net.alishahidi.mcpconductor.util.PlatformDetector;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cross-platform package management service supporting Linux, Windows, and macOS package managers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final SSHService sshService;
    private final PlatformDetector platformDetector;

    public String installPackage(String serverName, String packageName, String packageManager) {
        validatePackageName(packageName);
        validatePackageManager(packageManager);

        log.info("Installing package {} on server {} using {}", packageName, serverName, packageManager);

        String command = getInstallCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, true);

        if (result.isSuccess()) {
            log.info("Package {} installed successfully on {}", packageName, serverName);
            return "Package " + packageName + " installed successfully";
        } else {
            log.error("Failed to install package {}: {}", packageName, result.getError());
            throw new CommandExecutionException(
                    "Failed to install package: " + result.getError(),
                    command,
                    serverName,
                    result.getExitCode()
            );
        }
    }

    public String uninstallPackage(String serverName, String packageName, String packageManager) {
        validatePackageName(packageName);
        validatePackageManager(packageManager);

        log.info("Uninstalling package {} from server {} using {}", packageName, serverName, packageManager);

        String command = getUninstallCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, true);

        if (result.isSuccess()) {
            log.info("Package {} uninstalled successfully from {}", packageName, serverName);
            return "Package " + packageName + " uninstalled successfully";
        } else {
            log.error("Failed to uninstall package {}: {}", packageName, result.getError());
            throw new CommandExecutionException(
                    "Failed to uninstall package: " + result.getError(),
                    command,
                    serverName,
                    result.getExitCode()
            );
        }
    }

    public String updatePackages(String serverName, String packageManager) {
        validatePackageManager(packageManager);

        log.info("Updating packages on server {} using {}", serverName, packageManager);

        String command = getUpdateCommand(packageManager);
        CommandResult result = sshService.executeCommand(serverName, command, true);

        if (result.isSuccess()) {
            log.info("Packages updated successfully on {}", serverName);
            return "Packages updated successfully";
        } else {
            log.error("Failed to update packages on {}: {}", serverName, result.getError());
            throw new CommandExecutionException(
                    "Failed to update packages: " + result.getError(),
                    command,
                    serverName,
                    result.getExitCode()
            );
        }
    }

    public List<String> searchPackages(String serverName, String query, String packageManager) {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("query", query, "Search query cannot be empty");
        }
        validatePackageManager(packageManager);

        log.info("Searching packages with query '{}' on server {} using {}", query, serverName, packageManager);

        String command = getSearchCommand(packageManager, query);
        CommandResult result = sshService.executeCommand(serverName, command, false);

        if (result.isSuccess()) {
            return Arrays.stream(result.getOutput().split("\n"))
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        } else {
            log.error("Failed to search packages: {}", result.getError());
            throw new CommandExecutionException(
                    "Failed to search packages: " + result.getError(),
                    command,
                    serverName,
                    result.getExitCode()
            );
        }
    }
    
    public boolean isPackageInstalled(String serverName, String packageName, String packageManager) {
        validatePackageName(packageName);
        validatePackageManager(packageManager);

        String command = getCheckInstalledCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, false);
        return result.isSuccess() && result.getExitCode() == 0;
    }

    private void validatePackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new ValidationException("packageName", packageName, "Package name cannot be empty");
        }
        // Prevent command injection in package name
        if (packageName.contains(";") || packageName.contains("|") || packageName.contains("&")) {
            throw new ValidationException("packageName", packageName,
                    "Package name contains invalid characters");
        }
    }

    private void validatePackageManager(String packageManager) {
        if (packageManager == null || packageManager.trim().isEmpty()) {
            throw new ValidationException("packageManager", packageManager,
                    "Package manager cannot be empty");
        }
    }

    private String getInstallCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            // Linux package managers
            case "apt" -> "DEBIAN_FRONTEND=noninteractive apt-get update && apt-get install -y " + packageName;
            case "yum" -> "yum install -y " + packageName;
            case "dnf" -> "dnf install -y " + packageName;
            case "pacman" -> "pacman -S --noconfirm " + packageName;
            case "zypper" -> "zypper install -y " + packageName;
            // macOS package manager
            case "brew" -> "brew install " + packageName;
            // Windows package managers
            case "winget" -> "winget install --id " + packageName + " --silent --accept-package-agreements";
            case "choco", "chocolatey" -> "choco install " + packageName + " -y";
            case "scoop" -> "scoop install " + packageName;
            default -> throw new ValidationException("packageManager", packageManager,
                    "Unsupported package manager. Supported: apt, yum, dnf, pacman, zypper, brew, winget, choco, scoop");
        };
    }

    private String getUninstallCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            // Linux package managers
            case "apt" -> "apt-get remove -y " + packageName;
            case "yum" -> "yum remove -y " + packageName;
            case "dnf" -> "dnf remove -y " + packageName;
            case "pacman" -> "pacman -R --noconfirm " + packageName;
            case "zypper" -> "zypper remove -y " + packageName;
            // macOS package manager
            case "brew" -> "brew uninstall " + packageName;
            // Windows package managers
            case "winget" -> "winget uninstall --id " + packageName + " --silent";
            case "choco", "chocolatey" -> "choco uninstall " + packageName + " -y";
            case "scoop" -> "scoop uninstall " + packageName;
            default -> throw new ValidationException("packageManager", packageManager,
                    "Unsupported package manager");
        };
    }

    private String getUpdateCommand(String packageManager) {
        return switch (packageManager.toLowerCase()) {
            // Linux package managers
            case "apt" -> "DEBIAN_FRONTEND=noninteractive apt-get update && apt-get upgrade -y";
            case "yum" -> "yum update -y";
            case "dnf" -> "dnf update -y";
            case "pacman" -> "pacman -Syu --noconfirm";
            case "zypper" -> "zypper update -y";
            // macOS package manager
            case "brew" -> "brew update && brew upgrade";
            // Windows package managers
            case "winget" -> "winget upgrade --all --silent";
            case "choco", "chocolatey" -> "choco upgrade all -y";
            case "scoop" -> "scoop update *";
            default -> throw new ValidationException("packageManager", packageManager,
                    "Unsupported package manager");
        };
    }

    private String getSearchCommand(String packageManager, String query) {
        return switch (packageManager.toLowerCase()) {
            // Linux package managers
            case "apt" -> "apt-cache search " + query;
            case "yum" -> "yum search " + query;
            case "dnf" -> "dnf search " + query;
            case "pacman" -> "pacman -Ss " + query;
            case "zypper" -> "zypper search " + query;
            // macOS package manager
            case "brew" -> "brew search " + query;
            // Windows package managers
            case "winget" -> "winget search " + query;
            case "choco", "chocolatey" -> "choco search " + query;
            case "scoop" -> "scoop search " + query;
            default -> throw new ValidationException("packageManager", packageManager,
                    "Unsupported package manager");
        };
    }

    private String getCheckInstalledCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            // Linux package managers
            case "apt" -> "dpkg -l | grep -q '^ii.*" + packageName + "'";
            case "yum", "dnf" -> "rpm -qa | grep -q " + packageName;
            case "pacman" -> "pacman -Q " + packageName + " > /dev/null 2>&1";
            case "zypper" -> "zypper se -i " + packageName + " | grep -q '^i'";
            // macOS package manager
            case "brew" -> "brew list " + packageName + " > /dev/null 2>&1";
            // Windows package managers
            case "winget" -> "winget list --id " + packageName + " > $null 2>&1";
            case "choco", "chocolatey" -> "choco list --local-only " + packageName + " | findstr /C:\"" + packageName + "\"";
            case "scoop" -> "scoop list " + packageName + " > $null 2>&1";
            default -> throw new ValidationException("packageManager", packageManager,
                    "Unsupported package manager");
        };
    }
}
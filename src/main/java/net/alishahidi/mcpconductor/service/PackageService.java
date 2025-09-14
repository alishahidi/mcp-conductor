package net.alishahidi.mcpconductor.service;

import net.alishahidi.mcpconductor.model.CommandResult;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {
    
    private final SSHService sshService;
    
    public String installPackage(String serverName, String packageName, String packageManager) {
        String command = getInstallCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, true);
        
        if (result.isSuccess()) {
            return "Package " + packageName + " installed successfully";
        } else {
            throw new RuntimeException("Failed to install package: " + result.getError());
        }
    }
    
    public String uninstallPackage(String serverName, String packageName, String packageManager) {
        String command = getUninstallCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, true);
        
        if (result.isSuccess()) {
            return "Package " + packageName + " uninstalled successfully";
        } else {
            throw new RuntimeException("Failed to uninstall package: " + result.getError());
        }
    }
    
    public String updatePackages(String serverName, String packageManager) {
        String command = getUpdateCommand(packageManager);
        CommandResult result = sshService.executeCommand(serverName, command, true);
        
        if (result.isSuccess()) {
            return "Packages updated successfully";
        } else {
            throw new RuntimeException("Failed to update packages: " + result.getError());
        }
    }
    
    public List<String> searchPackages(String serverName, String query, String packageManager) {
        String command = getSearchCommand(packageManager, query);
        CommandResult result = sshService.executeCommand(serverName, command, false);
        
        if (result.isSuccess()) {
            return Arrays.asList(result.getOutput().split("\n"));
        } else {
            throw new RuntimeException("Failed to search packages: " + result.getError());
        }
    }
    
    public boolean isPackageInstalled(String serverName, String packageName, String packageManager) {
        String command = getCheckInstalledCommand(packageManager, packageName);
        CommandResult result = sshService.executeCommand(serverName, command, false);
        return result.isSuccess() && result.getExitCode() == 0;
    }
    
    private String getInstallCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            case "apt" -> "apt-get update && apt-get install -y " + packageName;
            case "yum" -> "yum install -y " + packageName;
            case "dnf" -> "dnf install -y " + packageName;
            case "pacman" -> "pacman -S --noconfirm " + packageName;
            case "brew" -> "brew install " + packageName;
            default -> throw new IllegalArgumentException("Unsupported package manager: " + packageManager);
        };
    }
    
    private String getUninstallCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            case "apt" -> "apt-get remove -y " + packageName;
            case "yum" -> "yum remove -y " + packageName;
            case "dnf" -> "dnf remove -y " + packageName;
            case "pacman" -> "pacman -R --noconfirm " + packageName;
            case "brew" -> "brew uninstall " + packageName;
            default -> throw new IllegalArgumentException("Unsupported package manager: " + packageManager);
        };
    }
    
    private String getUpdateCommand(String packageManager) {
        return switch (packageManager.toLowerCase()) {
            case "apt" -> "apt-get update && apt-get upgrade -y";
            case "yum" -> "yum update -y";
            case "dnf" -> "dnf update -y";
            case "pacman" -> "pacman -Syu --noconfirm";
            case "brew" -> "brew update && brew upgrade";
            default -> throw new IllegalArgumentException("Unsupported package manager: " + packageManager);
        };
    }
    
    private String getSearchCommand(String packageManager, String query) {
        return switch (packageManager.toLowerCase()) {
            case "apt" -> "apt-cache search " + query;
            case "yum" -> "yum search " + query;
            case "dnf" -> "dnf search " + query;
            case "pacman" -> "pacman -Ss " + query;
            case "brew" -> "brew search " + query;
            default -> throw new IllegalArgumentException("Unsupported package manager: " + packageManager);
        };
    }
    
    private String getCheckInstalledCommand(String packageManager, String packageName) {
        return switch (packageManager.toLowerCase()) {
            case "apt" -> "dpkg -l | grep -q '^ii.*" + packageName + "'";
            case "yum", "dnf" -> "rpm -qa | grep -q " + packageName;
            case "pacman" -> "pacman -Q " + packageName;
            case "brew" -> "brew list | grep -q " + packageName;
            default -> throw new IllegalArgumentException("Unsupported package manager: " + packageManager);
        };
    }
}
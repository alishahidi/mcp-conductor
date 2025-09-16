package net.alishahidi.mcpconductor.exception;

public class ConfigurationException extends RuntimeException {

    private final String configKey;
    private final String configFile;

    public ConfigurationException(String message) {
        super(message);
        this.configKey = null;
        this.configFile = null;
    }

    public ConfigurationException(String message, String configKey, String configFile) {
        super(String.format("Configuration error in %s - Key: %s - %s", configFile, configKey, message));
        this.configKey = configKey;
        this.configFile = configFile;
    }

    // Getters
    public String getConfigKey() { return configKey; }
    public String getConfigFile() { return configFile; }
}
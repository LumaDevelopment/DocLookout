package me.joshuasheldon.doclookout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

/**
 * Creates the configuration file if it doesn't already exist,
 * and creates Configuration objects from the configuration file
 * if it does exist.
 */
public class ConfigurationMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the configuration file.
     */
    public static final String CONFIGURATION_FILE_NAME = "config.json";

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The file where we store program configuration.
     */
    private final File configFile;

    /**
     * The object we use to read and write JSON.
     */
    private final ObjectMapper objectMapper;

    /* ---------- CONSTRUCTORS ---------- */

    private ConfigurationMgr() {
        this.configFile = new File(ConfigurationMgr.CONFIGURATION_FILE_NAME);
        this.objectMapper = new ObjectMapper();
    }

    /* ---------- SINGLETON ---------- */

    private static ConfigurationMgr instance;

    public static ConfigurationMgr getInstance() {

        if (instance == null) {
            instance = new ConfigurationMgr();
        }

        return instance;

    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * @return The configuration object, or <code>null</code>
     * if the configuration file does not exist or if there
     * was an issue loading it.
     */
    public synchronized Configuration getConfiguration() {

        if (!this.configFile.exists()) {
            // Attempt to create default configuration
            // file and then stop execution.
            createDefaultConfigurationFile();
            return null;
        }

        Configuration config;

        try {
            config = this.objectMapper.readValue(this.configFile, Configuration.class);
        } catch (Exception e) {
            System.err.println("Failed to read configuration from config file!");
            e.printStackTrace();
            return null;
        }

        return config;

    }

    /* ---------- PRIVATE METHODS ---------- */

    /**
     * Deletes the current configuration file (if it exists)
     * and writes a new one using the default values.
     */
    private void createDefaultConfigurationFile() {

        // Attempt to delete the current configuration file,
        // if it exists
        if (this.configFile.exists()) {
            try {
                this.configFile.delete();
            } catch (Exception e) {
                System.err.println("Failed to delete existing configuration file!");
                e.printStackTrace();
                return;
            }
        }

        // Create default configuration object
        Configuration config = Configuration.getDefault();

        // Attempt to write default configuration to file
        try {
            this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(this.configFile, config);
        } catch (Exception e) {
            System.err.println("Failed to write default configuration to config file!");
            e.printStackTrace();
            return;
        }

        System.out.println("Successfully created default configuration file!");

    }

}

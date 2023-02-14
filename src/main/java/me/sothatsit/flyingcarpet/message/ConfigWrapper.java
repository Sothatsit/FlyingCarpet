package me.sothatsit.flyingcarpet.message;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigWrapper {

    private Plugin plugin;
    private FileConfiguration config = null;
    private File configFile = null;
    private String name;
    private boolean autoReload;
    private long lastEdit;

    public ConfigWrapper(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.configFile = new File(plugin.getDataFolder(), name);
        this.autoReload = true;
        this.reload();
    }

    public ConfigWrapper(File file) {
        this.configFile = file;
        this.reload();
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        lastEdit = configFile.lastModified();

        Reader defConfigStream = null;
        defConfigStream = new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8);

        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null || (autoReload && configFile.lastModified() != lastEdit)) {
            reload();
        }

        return config;
    }

    public void save() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config " + configFile, ex);
        }
    }

    public void saveDefaults() {
        if (plugin != null) {
            if (!configFile.exists()) {
                plugin.saveResource(name, false);
            }

            reload();
        }
    }

    public FileConfiguration getDefaultConfig() {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8));
    }
}

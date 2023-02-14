package me.sothatsit.flyingcarpet.message;

import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

    private static ConfigWrapper config = null;

    public static Message get(String key) {
        if (config == null) {
            return new Message(key);
        }

        Message message = new Message(key);

        if (config.getConfig().isConfigurationSection(key)) {
            key += ".default";
        }

        if (config.getConfig().isString(key)) {
            message = new Message(key, config.getConfig().getString(key));
        } else if (config.getConfig().isList(key)) {
            message = new Message(key, config.getConfig().getStringList(key));
        }

        return message;
    }

    public static void setConfig(ConfigWrapper config) {
        Messages.config = config;

        reload();
    }

    public static void reload() {
        if (config == null) {
            return;
        }

        boolean changed = false;
        FileConfiguration defaults = config.getDefaultConfig();
        FileConfiguration conf = config.getConfig();

        for (String key : defaults.getKeys(true)) {
            if (!conf.isSet(key)) {
                if (conf.isConfigurationSection(key)) {
                    conf.createSection(key);
                    changed = true;
                    continue;
                }

                conf.set(key, defaults.get(key));
                changed = true;
            }
        }

        if (changed) {
            config.save();
        }
    }
}

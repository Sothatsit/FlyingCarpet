package me.sothatsit.flyingcarpet;

import me.sothatsit.flyingcarpet.message.ConfigWrapper;
import me.sothatsit.flyingcarpet.model.Model;
import me.sothatsit.flyingcarpet.model.BlockData;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class FCConfig {

    private Model base;
    private Model tools;
    private Model light;
    private Set<BlockData> passThrough;
    private int descendSpeed;

    private Set<String> worldguardRegionBlacklist = new HashSet<>();

    public Model getBaseModel() {
        return base;
    }

    public Model getToolsModel() {
        return tools;
    }

    public Model getLightModel() {
        return light;
    }

    public boolean canPassThrough(Block block) {
        return passThrough.contains(BlockData.fromBlock(block));
    }

    public int getDescendSpeed() {
        return descendSpeed;
    }

    public ConfigWrapper loadConfig() {
        ConfigWrapper configWrapper = new ConfigWrapper(FlyingCarpet.getInstance(), "config.yml");

        configWrapper.saveDefaults();
        configWrapper.reload();

        return configWrapper;
    }

    public void reloadConfiguration() {
        AtomicBoolean requiresSave = new AtomicBoolean(false);

        ConfigWrapper configWrapper = loadConfig();
        ConfigurationSection defaults = configWrapper.getDefaultConfig();
        ConfigurationSection config = configWrapper.getConfig();

        if (!config.isSet("pass-through") || !config.isList("pass-through")) {
            FlyingCarpet.warning("\"pass-through\" not set or invalid in config, resetting to default");
            config.set("pass-through", defaults.get("pass-through"));
            requiresSave.set(true);
        }

        if (!config.isSet("descend-speed") || !config.isInt("descend-speed")) {
            FlyingCarpet.warning("\"descend-speed\" not set or invalid in config, resetting to default");
            config.set("descend-speed", defaults.get("descend-speed"));
            requiresSave.set(true);
        }

        descendSpeed = config.getInt("descend-speed");

        passThrough = new HashSet<>();
        BlockData.AIR.addToSet(passThrough);

        List<String> passThroughStrings = config.getStringList("pass-through");
        List<String> passThroughReformatted = new ArrayList<>(passThroughStrings);

        for (int index = 0; index < passThroughStrings.size(); ++index) {
            String string = passThroughStrings.get(index);

            if (string.equalsIgnoreCase("water")) {
                BlockData.WATER.addToSet(passThrough);
                BlockData.STATIONARY_WATER.addToSet(passThrough);
                continue;
            }

            if (string.equalsIgnoreCase("lava")) {
                BlockData.LAVA.addToSet(passThrough);
                BlockData.STATIONARY_LAVA.addToSet(passThrough);
                continue;
            }

            try {
                AtomicBoolean requiresReformat = new AtomicBoolean(false);

                BlockData blockData = BlockData.fromString(string, requiresReformat);

                if(requiresReformat.get()) {
                    passThroughReformatted.set(index, blockData.toString());
                    requiresSave.set(true);

                    FlyingCarpet.info("1.13 Prep - " + string + " converted to " +
                                      blockData + " for pass-through in the config.yml");
                }

                blockData.addToSet(passThrough);
            } catch (BlockData.BlockDataParseException e) {
                FlyingCarpet.severe("Invalid pass through block " + string + ", " + e.getMessage());
            }
        }

        if(requiresSave.get()) {
            config.set("pass-through", passThroughReformatted);
        }

        if (!config.isConfigurationSection("model")) {
            FlyingCarpet.warning("\"model\" not set or invalid in config, resetting to default");
            config.set("model", null);

            ConfigurationSection sec = config.createSection("model");
            copySection(defaults, config, sec, requiresSave);
        }

        ConfigurationSection model = config.getConfigurationSection("model");

        if (!model.isSet("base")) {
            FlyingCarpet.warning("\"model.base\" not set or invalid in config, resetting to default");
            model.set("base", null);

            ConfigurationSection sec = model.createSection("base");
            copySection(defaults, config, sec, requiresSave);
        }

        if (!model.isSet("tools")) {
            FlyingCarpet.warning("\"model.tools\" not set or invalid in config, resetting to default");
            model.set("tools", null);

            ConfigurationSection sec = model.createSection("tools");
            copySection(defaults, config, sec, requiresSave);
        }

        if (!model.isSet("light")) {
            FlyingCarpet.warning("\"model.light\" not set or invalid in config, resetting to default");
            model.set("light", null);

            ConfigurationSection sec = model.createSection("light");
            copySection(defaults, config, sec, requiresSave);
        }

        ConfigurationSection baseSec = model.getConfigurationSection("base");
        ConfigurationSection toolsSec = model.getConfigurationSection("tools");
        ConfigurationSection lightSec = model.getConfigurationSection("light");

        base = Model.fromConfig(baseSec, requiresSave);
        tools = Model.fromConfig(toolsSec, requiresSave);
        light = Model.fromConfig(lightSec, requiresSave);

        if(FlyingCarpet.isWorldGuardHooked()) {
            reloadWorldguardConfiguration(config);
        }

        if(requiresSave.get()) {
            configWrapper.save();
        }
    }

    private static void copySection(ConfigurationSection defaults,
                                    ConfigurationSection config,
                                    ConfigurationSection section,
                                    AtomicBoolean requiresSave) {

        for (String key : defaults.getKeys(true)) {
            if(!key.startsWith(section.getCurrentPath()) || config.isSet(key))
                continue;

            if (defaults.isConfigurationSection(key)) {
                config.createSection(key);
                requiresSave.set(true);
                continue;
            }

            config.set(key, defaults.get(key));
            requiresSave.set(true);
        }
    }

    public void reloadWorldguardConfiguration(ConfigurationSection config) {
        if(!config.isSet("wg-region-blacklist") || !config.isList("wg-region-blacklist")) {
            config.set("wg-region-blacklist", new ArrayList<>(worldguardRegionBlacklist));
        }

        for(String region : config.getStringList("wg-region-blacklist")) {
            worldguardRegionBlacklist.add(region.toLowerCase());
        }

        FlyingCarpet.info("Loaded " + worldguardRegionBlacklist.size() + " blacklisted WorldGuard regions");
    }

    public void addWorldguardBlacklistedRegion(String region) {
        worldguardRegionBlacklist.add(region.toLowerCase());

        ConfigWrapper wrapper = loadConfig();
        wrapper.getConfig().set("wg-region-blacklist", new ArrayList<>(worldguardRegionBlacklist));
        wrapper.save();
    }

    public void removeWorldguardBlacklistedRegion(String region) {
        worldguardRegionBlacklist.remove(region.toLowerCase());

        ConfigWrapper wrapper = loadConfig();
        wrapper.getConfig().set("wg-region-blacklist", new ArrayList<>(worldguardRegionBlacklist));
        wrapper.save();
    }

    public Set<String> getWorldguardBlacklistedRegions() {
        return worldguardRegionBlacklist;
    }

}

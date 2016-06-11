package me.sothatsit.flyingcarpet;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.sothatsit.flyingcarpet.message.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WorldGuardHook implements RegionHook {

    private WorldGuardPlugin hook = null;
    private Set<String> regionBlacklist;

    public WorldGuardHook() {
        this.hook = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.regionBlacklist = new HashSet<>();

        FlyingCarpet.getInstance().getLogger().info("Hooked WorldGuard");
    }

    @Override
    public boolean isCarpetAllowed(Location loc) {
        RegionManager manager = this.hook.getRegionManager(loc.getWorld());

        Iterator<String> iter = this.regionBlacklist.iterator();

        while(iter.hasNext()) {
            ProtectedRegion region = manager.getRegion(iter.next());

            if(region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void reloadConfiguration(FileConfiguration config) {
        if(!config.isSet("wg-region-blacklist") || !config.isList("wg-region-blacklist")) {
            config.set("wg-region-blacklist", new ArrayList<>(regionBlacklist));
        }

        this.regionBlacklist.addAll(config.getStringList("wg-region-blacklist"));

        FlyingCarpet.getInstance().getLogger().info("Loaded " + this.regionBlacklist.size() + " blacklisted WorldGuard regions");
    }

    @Override
    public void addBlacklistedRegion(String region) {
        this.regionBlacklist.add(region.toLowerCase());

        ConfigWrapper wrapper = FlyingCarpet.getInstance().loadConfig();
        wrapper.getConfig().set("wg-region-blacklist", new ArrayList<>(this.regionBlacklist));
        wrapper.save();
    }

    @Override
    public void removeBlacklistedRegion(String region) {
        this.regionBlacklist.remove(region.toLowerCase());

        ConfigWrapper wrapper = FlyingCarpet.getInstance().loadConfig();
        wrapper.getConfig().set("wg-region-blacklist", new ArrayList<>(this.regionBlacklist));
        wrapper.save();
    }

    @Override
    public Set<String> getBlacklistedRegions() {
        return this.regionBlacklist;
    }
}

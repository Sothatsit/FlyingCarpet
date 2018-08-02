package me.sothatsit.flyingcarpet;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.sothatsit.flyingcarpet.message.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldGuardHook {

    private WorldGuardPlugin hook = null;

    public WorldGuardHook() {
        this.hook = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        FlyingCarpet.info("Hooked WorldGuard");
    }

    public boolean isCarpetAllowed(Location loc) {
        RegionManager manager = hook.getRegionManager(loc.getWorld());

        for(String aRegionBlacklist : FlyingCarpet.getMainConfig().getWorldguardBlacklistedRegions()) {
            ProtectedRegion region = manager.getRegion(aRegionBlacklist);

            if (region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                return false;
        }

        return true;
    }

}

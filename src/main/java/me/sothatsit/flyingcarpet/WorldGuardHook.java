package me.sothatsit.flyingcarpet;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Location;

public class WorldGuardHook {

    public boolean isCarpetAllowed(Location loc) {
    	RegionContainer hook = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = hook.get(BukkitAdapter.adapt(loc.getWorld()));
        if(manager == null)
            return true;

        for(String regionName : FlyingCarpet.getMainConfig().getWorldguardBlacklistedRegions()) {
            ProtectedRegion region = manager.getRegion(regionName);

            if (region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                return false;
        }

        return true;
    }
}

package me.sothatsit.flyingcarpet.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.sothatsit.flyingcarpet.FlyingCarpet;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class WorldGuardHook implements PluginHooks {
    @Override
    public String hookName() {
        return "WorldGuard";
    }

    @Override
    public boolean isCarpetAllowed(Location loc) {
        RegionContainer hook = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = hook.get(BukkitAdapter.adapt(loc.getWorld()));
        if (manager == null)
            return true;

        for (String regionName : FlyingCarpet.getMainConfig().getWorldguardBlacklistedRegions()) {
            ProtectedRegion region = manager.getRegion(regionName);

            if (region != null && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                return false;
        }

        return true;
    }

    @Override
    public boolean canBuild(Player player) {
        RegionContainer hook = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = hook.get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null)
            return true;

        for (ProtectedRegion r :manager.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ())).getRegions()) {
            if (r.isMember(player.getName()) || r.isOwner(player.getName())) {
                return true;
            }
        }
        return false;
    }
}

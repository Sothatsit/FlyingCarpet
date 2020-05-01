package me.sothatsit.flyingcarpet.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RedProtectHook implements PluginHooks {
    @Override
    public String hookName() {
        return "RedProtect";
    }

    @Override
    public boolean isCarpetAllowed(Location loc) {
        Region r = RedProtect.get().rm.getTopRegion(loc);
        if (r == null) return true;

        return r.getFlagBool("allow-magiccarpet");
    }

    @Override
    public boolean canBuild(Player player) {
        Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        if (r == null) return true;

        return r.canBuild(player);
    }
}

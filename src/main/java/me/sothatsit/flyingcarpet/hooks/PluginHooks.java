package me.sothatsit.flyingcarpet.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PluginHooks {

    String hookName();

    boolean isCarpetAllowed(Location loc);

    boolean canBuild(Player player);
}

package me.sothatsit.flyingcarpet;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public interface RegionHook {

    public boolean isCarpetAllowed(Location loc);

    public void reloadConfiguration(FileConfiguration config);

    public void addBlacklistedRegion(String region);

    public void removeBlacklistedRegion(String region);

    public Set<String> getBlacklistedRegions();

}

package me.sothatsit.flyingcarpet.util;

import org.bukkit.Location;

public class Region {
    
    private Vector3I min;
    private Vector3I max;
    
    public Region(Vector3I from, Vector3I to) {
        min = new Vector3I(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        max = new Vector3I(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
    }
    
    public Vector3I getMin() {
        return min;
    }
    
    public Vector3I getMax() {
        return max;
    }
    
    public boolean inBounds(Location loc) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() + 1
                && loc.getY() >= min.getY() && loc.getY() <= max.getY() + 1
                && loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ() + 1;
    }
    
    public static Region combine(Region... regions) {
        if (regions.length == 0)
            return null;
        
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int minz = Integer.MAX_VALUE;
        
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int maxz = Integer.MIN_VALUE;
        
        for (Region r : regions) {
            minx = Math.min(r.getMin().getX(), minx);
            miny = Math.min(r.getMin().getY(), miny);
            minz = Math.min(r.getMin().getZ(), minz);
            
            maxx = Math.max(r.getMax().getX(), maxx);
            maxy = Math.max(r.getMax().getY(), maxy);
            maxz = Math.max(r.getMax().getZ(), maxz);
        }
        
        return new Region(new Vector3I(minx, miny, minz), new Vector3I(maxx, maxy, maxz));
    }
    
}

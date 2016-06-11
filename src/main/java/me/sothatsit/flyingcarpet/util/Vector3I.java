package me.sothatsit.flyingcarpet.util;

import org.bukkit.configuration.ConfigurationSection;

public class Vector3I {
    
    private int x;
    private int y;
    private int z;
    
    public Vector3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public void setZ(int z) {
        this.z = z;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3I))
            return super.equals(obj);
        
        Vector3I other = (Vector3I) obj;
        
        return other.x == x && other.y == y && other.z == z;
    }
    
    public static Vector3I fromConfig(ConfigurationSection section) {
        if (!section.isInt("x") || !section.isInt("y") || !section.isInt("z"))
            return null;
        
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        
        return new Vector3I(x, y, z);
    }
    
}

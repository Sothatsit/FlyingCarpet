package me.sothatsit.flyingcarpet.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class BlockData {
    
    public static final BlockData AIR = new BlockData(Material.AIR, (byte) 0);
    
    private Material type;
    private byte data;
    
    public BlockData(Material type) {
        this(type, (byte) -1);
    }
    
    public BlockData(Material type, byte data) {
        this.type = type;
        this.data = data;
    }
    
    public Material getType() {
        return type;
    }
    
    public byte getData() {
        return data;
    }
    
    public void setType(Material type) {
        this.type = type;
    }
    
    public void setData(byte data) {
        this.data = data;
    }
    
    @SuppressWarnings("deprecation")
    public void apply(Block b) {
        if (b.getType() == type && b.getData() == data)
            return;
        
        b.setTypeIdAndData(type.getId(), (data < 0 ? 0 : data), false);
    }
    
    @SuppressWarnings("deprecation")
    public static BlockData fromSection(ConfigurationSection section) {
        if (!section.isInt("block-type") || !section.isInt("block-data"))
            return null;
        
        Material type = Material.getMaterial(section.getInt("block-type"));
        byte data = (byte) section.getInt("block-data");
        
        if (type == null)
            return null;
        
        return new BlockData(type, data);
    }
    
}

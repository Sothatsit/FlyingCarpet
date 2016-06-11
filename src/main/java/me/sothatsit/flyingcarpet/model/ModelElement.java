package me.sothatsit.flyingcarpet.model;

import java.util.List;

import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.Region;
import me.sothatsit.flyingcarpet.util.Vector3I;

import org.bukkit.Location;
import org.bukkit.block.Block;

public abstract class ModelElement {
    
    private BlockData blockData;
    
    public ModelElement(BlockData blockData) {
        this.blockData = blockData;
    }
    
    public BlockData getBlockData() {
        return blockData;
    }
    
    public void setBlockData(BlockData blockData) {
        this.blockData = blockData;
    }
    
    public abstract List<Block> placeElement(Location loc);
    
    public abstract boolean inBounds(Vector3I offset);
    
    public abstract Region getRegion();
    
}

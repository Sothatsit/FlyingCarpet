package me.sothatsit.flyingcarpet.model;

import java.util.ArrayList;
import java.util.List;

import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.Region;
import me.sothatsit.flyingcarpet.util.Vector3I;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockElement extends ModelElement {
    
    private Vector3I offset;
    
    public BlockElement(BlockData blockData, Vector3I offset) {
        super(blockData);
        this.offset = offset;
    }
    
    public Vector3I getOffset() {
        return offset;
    }
    
    @Override
    public List<Block> placeElement(Location loc) {
        List<Block> blocks = new ArrayList<Block>();
        
        Block b = loc.clone().add(offset.getX(), offset.getY(), offset.getZ()).getBlock();
        
        blocks.add(b);
        
        this.getBlockData().apply(b);
        
        return null;
    }
    
    @Override
    public boolean inBounds(Vector3I offset) {
        return this.offset.equals(offset);
    }
    
    @Override
    public Region getRegion() {
        return new Region(offset, offset);
    }
    
}

package me.sothatsit.flyingcarpet.model;

import java.util.ArrayList;
import java.util.List;

import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.Region;
import me.sothatsit.flyingcarpet.util.Vector3I;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class CuboidElement extends ModelElement {
    
    private Vector3I from;
    private Vector3I to;
    
    public CuboidElement(BlockData blockData, Vector3I from, Vector3I to) {
        super(blockData);
        this.from = from;
        this.to = to;
    }
    
    public Vector3I getFrom() {
        return from;
    }
    
    public Vector3I getTo() {
        return to;
    }
    
    @Override
    public List<Block> placeElement(Location loc) {
        List<Block> blocks = new ArrayList<Block>();
        
        for (int x = from.getX(); x <= to.getX(); x++) {
            for (int y = from.getY(); y <= to.getY(); y++) {
                for (int z = from.getZ(); z <= to.getZ(); z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    
                    blocks.add(b);
                    
                    this.getBlockData().apply(b);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public boolean inBounds(Vector3I offset) {
        return offset.getX() >= from.getX() && offset.getX() <= to.getX()
                && offset.getY() >= from.getY() && offset.getY() <= to.getY()
                && offset.getZ() >= from.getZ() && offset.getZ() <= to.getZ();
    }
    
    @Override
    public Region getRegion() {
        return new Region(from, to);
    }
    
}

package me.sothatsit.flyingcarpet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.sothatsit.flyingcarpet.model.Model;
import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.LocationUtils;
import me.sothatsit.flyingcarpet.util.Region;
import me.sothatsit.flyingcarpet.util.Vector3I;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UPlayer {
    
    private Player player;
    private Location loc;
    private boolean enabled;
    private boolean tools;
    private boolean light;
    private List<BlockState> blocks;
    
    private BukkitRunnable descendTimer;
    
    public UPlayer(Player player) {
        this.player = player;
        this.loc = player.getLocation().subtract(0, 1, 0);
        this.enabled = false;
        this.tools = false;
        this.light = false;
        this.blocks = new ArrayList<BlockState>();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getLocation() {
        return loc;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isTools() {
        return tools;
    }
    
    public boolean isLight() {
        return light;
    }
    
    public BukkitRunnable getDescendTimer() {
        return descendTimer;
    }
    
    public void cancelDescendTimer() {
        if (descendTimer != null) {
            descendTimer.cancel();
            descendTimer = null;
        }
    }
    
    public void createDescendTimer() {
        cancelDescendTimer();
        
        descendTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                
                setLocation(player.getLocation().subtract(0, 2, 0));
            }
        };
        
        descendTimer.runTaskTimer(FlyingCarpet.getInstance(), FlyingCarpet.getDescendSpeed(), FlyingCarpet.getDescendSpeed());
    }
    
    public boolean isCarpetBlock(Block b) {
        return isCarpetBlock(b.getLocation());
    }
    
    public boolean isCarpetBlock(Location loc) {
        for (BlockState state : blocks) {
            if (LocationUtils.locEqual(state.getLocation(), loc))
                return true;
        }
        return false;
    }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return;
        
        this.enabled = enabled;
        
        if (enabled)
            createCarpet();
        else
            removeCarpet();
    }
    
    public void setLocation(Location loc) {
        if (!enabled || LocationUtils.locEqual(this.loc, loc)) {
            this.loc = loc;
            return;
        }
        
        this.loc = loc;
        
        this.createCarpet();
    }
    
    public void setTools(boolean tools) {
        if (!enabled || this.tools == tools) {
            this.tools = tools;
            return;
        }
        
        this.tools = tools;
        
        this.createCarpet();
    }
    
    public void setLight(boolean light) {
        if (!enabled || this.light == light) {
            this.light = light;
            return;
        }
        
        this.light = light;
        
        this.createCarpet();
    }
    
    @SuppressWarnings("deprecation")
    public void removeCarpet() {
        for (BlockState state : blocks) {
            Block b = state.getBlock();
            
            b.setType(state.getType());
            b.setData(state.getRawData());
        }
        
        blocks = new ArrayList<BlockState>();
    }
    
    @SuppressWarnings("deprecation")
    public void createCarpet() {
        List<Model> models = new ArrayList<Model>();
        
        models.add(FlyingCarpet.getBaseModel());
        
        if (tools) {
            models.add(FlyingCarpet.getToolsModel());
        }
        
        if (light) {
            models.add(FlyingCarpet.getLightModel());
        }
        
        Region[] regions = new Region[models.size()];
        
        for (int i = 0; i < regions.length; i++) {
            regions[i] = models.get(i).getRegion();
        }
        
        Region region = Region.combine(regions);
        
        List<CarpetBlock> newBlocks = new ArrayList<CarpetBlock>();
        for (int x = region.getMin().getX(); x <= region.getMax().getX(); x++) {
            for (int y = region.getMin().getY(); y <= region.getMax().getY(); y++) {
                for (int z = region.getMin().getZ(); z <= region.getMax().getZ(); z++) {
                    Location l = loc.clone().add(x, y, z);
                    Vector3I offset = Model.getOffset(loc, l);
                    
                    BlockData data = BlockData.AIR;
                    
                    for (int i = 0; i < models.size(); i++) {
                        Model m = models.get(i);
                        
                        BlockData d = m.getBlockData(offset);
                        
                        if (d.getType() != Material.AIR)
                            data = d;
                    }
                    
                    if (data.getType() == Material.AIR)
                        continue;
                    
                    newBlocks.add(new CarpetBlock(data, l));
                }
            }
        }
        
        List<BlockState> states = new ArrayList<BlockState>();
        
        Iterator<BlockState> stateIterator = blocks.iterator();
        
        while (stateIterator.hasNext()) {
            BlockState state = stateIterator.next();
            
            boolean found = false;
            Iterator<CarpetBlock> carpetIterator = newBlocks.iterator();
            while (carpetIterator.hasNext()) {
                CarpetBlock block = carpetIterator.next();
                
                if (!LocationUtils.locEqual(state.getLocation(), block.loc))
                    continue;
                
                found = true;
                
                carpetIterator.remove();
                states.add(state);
                
                block.blockData.apply(state.getBlock());
                
                break;
            }
            
            if (!found) {
                Block b = state.getBlock();
                
                b.setTypeIdAndData(state.getTypeId(), state.getRawData(), false);
            }
        }
        
        Iterator<CarpetBlock> carpetIterator = newBlocks.iterator();
        while (carpetIterator.hasNext()) {
            CarpetBlock block = carpetIterator.next();
            
            Block b = block.loc.getBlock();
            
            if (!FlyingCarpet.canPassThrough(b.getType(), b.getData()))
                carpetIterator.remove();
        }
        
        for (CarpetBlock block : newBlocks) {
            Block b = block.loc.getBlock();
            
            states.add(b.getState());
            
            block.blockData.apply(b);
        }
        
        this.blocks = states;
    }
    
    private class CarpetBlock {
        
        public BlockData blockData;
        public Location loc;
        
        public CarpetBlock(BlockData blockData, Location loc) {
            this.blockData = blockData;
            this.loc = loc;
        }
        
    }
}

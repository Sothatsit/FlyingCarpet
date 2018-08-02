package me.sothatsit.flyingcarpet;

import java.util.*;

import me.sothatsit.flyingcarpet.message.Messages;
import me.sothatsit.flyingcarpet.model.Model;
import me.sothatsit.flyingcarpet.model.BlockData;
import me.sothatsit.flyingcarpet.model.Region;
import me.sothatsit.flyingcarpet.model.BlockOffset;

import me.sothatsit.flyingcarpet.util.Checks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UPlayer {
    
    private final Player player;
    private final Map<BlockOffset, BlockState> blocks;

    private Location loc;
    private boolean enabled;
    private boolean tools;
    private boolean light;
    
    private BukkitRunnable descendTimer;
    
    public UPlayer(Player player) {
        this.player = player;
        this.loc = player.getLocation().subtract(0, 1, 0);
        this.enabled = false;
        this.tools = false;
        this.light = false;
        this.blocks = new HashMap<>();
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
    
    public void stopDescent() {
        if(descendTimer == null)
            return;

        descendTimer.cancel();
        descendTimer = null;
    }
    
    public void startDescent() {
        stopDescent();
        
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

        int descendSpeed = FlyingCarpet.getMainConfig().getDescendSpeed();
        descendTimer.runTaskTimer(FlyingCarpet.getInstance(), descendSpeed, descendSpeed);
    }

    public boolean isCarpetBlock(Location location) {
        return isCarpetBlock(BlockOffset.fromLocation(location));
    }

    public boolean isCarpetBlock(Block block) {
        return isCarpetBlock(BlockOffset.fromBlock(block));
    }
    
    public boolean isCarpetBlock(BlockOffset location) {
        return blocks.containsKey(location);
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
        if (!enabled || BlockOffset.locEqual(this.loc, loc)) {
            this.loc = loc;
            return;
        }

        this.loc = loc;

        createCarpet();
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

    public void removeCarpet() {
        for(BlockState state : blocks.values()) {
            state.update(true, false);
        }

        blocks.clear();
    }

    public List<Model> getModels() {
        List<Model> models = new ArrayList<>(3);

        models.add(FlyingCarpet.getMainConfig().getBaseModel());

        if(tools)
            models.add(FlyingCarpet.getMainConfig().getToolsModel());
        if(light)
            models.add(FlyingCarpet.getMainConfig().getLightModel());

        return models;
    }

    public Region getCarpetRegion(List<Model> models) {
        Checks.ensureNonNull(models, "models");
        Checks.ensureTrue(models.size() > 0, "models cannot be empty");

        if(models.size() == 1)
            return models.get(0).region;

        List<Region> regions = new ArrayList<>(models.size());

        for(Model model : models) {
            regions.add(model.region);
        }

        return Region.combine(regions);
    }

    @SuppressWarnings("deprecation")
    public void createCarpet() {
        if(!FlyingCarpet.getInstance().isCarpetAllowed(player.getLocation())) {
            setEnabled(false);
            Messages.get("message.region-remove").send(player);
            return;
        }

        World world = loc.getWorld();
        FCConfig mainConfig = FlyingCarpet.getMainConfig();

        List<Model> models = getModels();
        Region region = getCarpetRegion(models);

        Map<BlockOffset, BlockData> modelBlocks = new HashMap<>();

        for (int x = region.min.x; x <= region.max.x; x++) {
            for (int y = region.min.y; y <= region.max.y; y++) {
                for (int z = region.min.z; z <= region.max.z; z++) {
                    BlockOffset offset = new BlockOffset(x, y, z);
                    BlockOffset location = new BlockOffset(
                            loc.getBlockX() + x,
                            loc.getBlockY() + y,
                            loc.getBlockZ() + z);

                    BlockData data = Model.getBlockData(models, offset);

                    if(data == BlockData.AIR)
                        continue;

                    modelBlocks.put(location, data);
                }
            }
        }

        List<BlockState> restore = new ArrayList<>();
        Iterator<Map.Entry<BlockOffset, BlockState>> placedIterator = blocks.entrySet().iterator();

        while(placedIterator.hasNext()) {
            Map.Entry<BlockOffset, BlockState> entry = placedIterator.next();

            BlockOffset location = entry.getKey();
            BlockState state = entry.getValue();

            BlockData data = modelBlocks.get(location);

            if(data == null) {
                restore.add(state);
                placedIterator.remove();
                continue;
            }

            data.apply(state.getBlock());
            modelBlocks.remove(location);
        }

        for(Map.Entry<BlockOffset, BlockData> entry : modelBlocks.entrySet()) {
            BlockOffset location = entry.getKey();
            Block block = location.getBlock(world);
            BlockData blockData = entry.getValue();

            if(!mainConfig.canPassThrough(block))
                continue;

            blocks.put(location, block.getState());
            blockData.apply(block);
        }

        for(BlockState state : restore) {
            state.update(true, false);
        }
    }
}

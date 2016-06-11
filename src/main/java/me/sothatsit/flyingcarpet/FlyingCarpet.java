package me.sothatsit.flyingcarpet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.sothatsit.flyingcarpet.message.ConfigWrapper;
import me.sothatsit.flyingcarpet.message.Messages;
import me.sothatsit.flyingcarpet.model.Model;
import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.LocationUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FlyingCarpet extends JavaPlugin implements Listener {
    
    private static FlyingCarpet instance;
    private Model base;
    private Model tools;
    private Model light;
    private List<BlockData> passThrough;
    private int descendSpeed;
    
    private List<UPlayer> players = new ArrayList<UPlayer>();
    
    @Override
    public void onEnable() {
        instance = this;
        
        getCommand("flyingcarpet").setExecutor(new FlyingCarpetCommand());
        Bukkit.getPluginManager().registerEvents(this, this);
        
        Messages.setConfig(new ConfigWrapper(this, "lang.yml"));
        
        reloadConfiguration();
    }
    
    @Override
    public void onDisable() {
        instance = null;
        
        for (UPlayer up : players) {
            up.setEnabled(false);
        }
    }
    
    @SuppressWarnings("deprecation")
    public void reloadConfiguration() {
        ConfigWrapper configWrapper = new ConfigWrapper(this, "config.yml");
        configWrapper.saveDefaults();
        configWrapper.reload();
        
        FileConfiguration config = configWrapper.getConfig();
        
        if (!config.isSet("pass-through") || !config.isList("pass-through")) {
            getLogger().warning("\"pass-through\" not set or invalid in config, resetting to default");
            config.set("pass-through", configWrapper.getDefaultConfig().get("pass-through"));
            configWrapper.save();
        }
        
        if (!config.isSet("descend-speed") || !config.isInt("descend-speed")) {
            getLogger().warning("\"descend-speed\" not set or invalid in config, resetting to default");
            config.set("descend-speed", configWrapper.getDefaultConfig().get("descend-speed"));
            configWrapper.save();
        }
        
        descendSpeed = config.getInt("descend-speed");
        
        List<String> passThrough = config.getStringList("pass-through");
        this.passThrough = new ArrayList<BlockData>();
        
        this.passThrough.add(BlockData.AIR);
        
        for (String str : passThrough) {
            if (str.equalsIgnoreCase("water")) {
                BlockData stillWater = new BlockData(Material.STATIONARY_WATER);
                BlockData runningWater = new BlockData(Material.WATER);
                
                this.passThrough.add(stillWater);
                this.passThrough.add(runningWater);
                
                continue;
            }
            
            if (str.equalsIgnoreCase("lava")) {
                BlockData stillLava = new BlockData(Material.STATIONARY_LAVA);
                BlockData runningLava = new BlockData(Material.LAVA);
                
                this.passThrough.add(stillLava);
                this.passThrough.add(runningLava);
                
                continue;
            }
            
            String[] split = str.split(":");
            
            if (split.length == 0) {
                getLogger().warning("Invalid pass through block \"" + str + "\"");
                continue;
            }
            
            int id;
            Material type;
            try {
                id = Integer.valueOf(split[0]);
                type = Material.getMaterial(id);
            } catch (NumberFormatException e) {
                getLogger().warning("Invalid pass through block \"" + str + "\", type must be an integer");
                continue;
            }
            
            if (type == null) {
                getLogger().warning("Invalid pass through block \"" + str + "\", invalid material \"" + id + "\"");
                continue;
            }
            
            if (split.length == 1) {
                this.passThrough.add(new BlockData(type));
                continue;
            }
        }
        
        if (!config.isConfigurationSection("model")) {
            getLogger().warning("\"model\" not set or invalid in config, resetting to default");
            config.set("model", null);
            
            ConfigurationSection sec = config.createSection("model");
            copySection(configWrapper, config, sec);
        }
        
        ConfigurationSection model = config.getConfigurationSection("model");
        
        if (!model.isSet("base")) {
            getLogger().warning("\"model.base\" not set or invalid in config, resetting to default");
            model.set("base", null);
            
            ConfigurationSection sec = model.createSection("base");
            copySection(configWrapper, config, sec);
        }
        
        if (!model.isSet("tools")) {
            getLogger().warning("\"model.tools\" not set or invalid in config, resetting to default");
            model.set("tools", null);
            
            ConfigurationSection sec = model.createSection("tools");
            copySection(configWrapper, config, sec);
        }
        
        if (!model.isSet("light")) {
            getLogger().warning("\"model.light\" not set or invalid in config, resetting to default");
            model.set("light", null);
            
            ConfigurationSection sec = model.createSection("light");
            copySection(configWrapper, config, sec);
        }
        
        ConfigurationSection baseSec = model.getConfigurationSection("base");
        ConfigurationSection toolsSec = model.getConfigurationSection("tools");
        ConfigurationSection lightSec = model.getConfigurationSection("light");
        
        base = Model.fromConfig(baseSec);
        tools = Model.fromConfig(toolsSec);
        light = Model.fromConfig(lightSec);
        
        configWrapper.save();
    }
    
    private static void copySection(ConfigWrapper config, FileConfiguration conf, ConfigurationSection section) {
        boolean changed = false;
        FileConfiguration defaults = config.getDefaultConfig();
        
        for (String key : defaults.getKeys(true)) {
            if (key.startsWith(section.getCurrentPath()) && !conf.isSet(key)) {
                if (conf.isConfigurationSection(key)) {
                    conf.createSection(key);
                    changed = true;
                    continue;
                }
                
                conf.set(key, defaults.get(key));
                changed = true;
            }
        }
        
        if (changed)
            config.save();
    }
    
    public UPlayer getUPlayer(Player p) {
        for (UPlayer up : players) {
            if (up.getPlayer().getUniqueId().equals(p.getUniqueId()))
                return up;
        }
        
        UPlayer up = new UPlayer(p);
        
        players.add(up);
        
        return up;
    }
    
    public UPlayer getUPlayer(UUID uuid) {
        for (UPlayer up : players) {
            if (up.getPlayer().getUniqueId().equals(uuid))
                return up;
        }
        return null;
    }
    
    public void removeUPlayer(Player p) {
        UPlayer up = getUPlayer(p);
        
        if (up == null)
            return;
        
        players.remove(up);
        
        up.setEnabled(false);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        removeUPlayer(e.getPlayer());
    }
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        removeUPlayer(e.getPlayer());
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        for (UPlayer up : players) {
            Player p = up.getPlayer();
            
            if (!up.isCarpetBlock(e.getBlock())) {
                if (up.isEnabled()) {
                    final UUID uuid = p.getUniqueId();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            UPlayer up = getUPlayer(uuid);
                            
                            if (up == null)
                                return;
                            
                            up.createCarpet();
                        }
                    }.runTask(this);
                }
                
                continue;
            }
            
            e.setCancelled(true);
            return;
        }
    }
    
    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            for (UPlayer up : players) {
                if (!up.isCarpetBlock(b))
                    continue;
                
                e.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent e) {
        for (UPlayer up : players) {
            if (!up.isCarpetBlock(e.getEntity().getLocation()))
                continue;
            
            e.setCancelled(true);
            return;
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        List<Block> remove = new ArrayList<Block>();
        
        for (Block b : e.blockList()) {
            for (UPlayer up : players) {
                Player p = up.getPlayer();
                
                if (!up.isCarpetBlock(b)) {
                    if (up.isEnabled()) {
                        final UUID uuid = p.getUniqueId();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                UPlayer up = getUPlayer(uuid);
                                
                                if (up == null)
                                    return;
                                
                                up.createCarpet();
                            }
                        }.runTask(this);
                    }
                    
                    continue;
                }
                
                remove.add(b);
            }
        }
        
        for (Block block : remove) {
            e.blockList().remove(block);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() != DamageCause.SUFFOCATION) {
            return;
        }
        
        Location[] locs;
        
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) e.getEntity();
            
            locs = new Location[] { le.getLocation(), le.getEyeLocation() };
        } else {
            locs = new Location[] { e.getEntity().getLocation() };
        }
        
        for (Location loc : locs) {
            for (UPlayer up : players) {
                if (up.isCarpetBlock(loc)) {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getCause() != DamageCause.FALL) {
            return;
        }
        
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        
        Player p = (Player) e.getEntity();
        UPlayer up = getUPlayer(p);
        
        if (up.isEnabled()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled() || LocationUtils.locEqual(e.getFrom(), e.getTo())) {
            return;
        }
        
        UPlayer up = getUPlayer(e.getPlayer());
        
        if (LocationUtils.locColumnEqual(e.getFrom(), e.getTo()) && Math.abs(e.getFrom().getY() - e.getTo().getY()) <= 2) {
            up.setLocation(e.getTo().clone().subtract(0, 1, 0));
            return;
        }
        
        if (up.isEnabled() && !e.getPlayer().hasPermission("flyingcarpet.teleport")) {
            up.setEnabled(false);
            Messages.get("message.teleport-remove").send(e.getPlayer());
        } else {
            up.setLocation(e.getTo().clone().subtract(0, 1, 0));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled() || LocationUtils.locEqual(e.getFrom(), e.getTo()))
            return;
        
        UPlayer up = getUPlayer(e.getPlayer());
        
        if (up.isEnabled() && up.getLocation().getBlockY() == e.getTo().getBlockY() && up.isCarpetBlock(e.getTo().getBlock())) {
            Location loc = e.getPlayer().getLocation();
            
            loc.setY(up.getLocation().getBlockY() + 1.2d);
            
            e.getPlayer().teleport(loc);
            return;
        }
        
        up.setLocation(e.getTo().clone().subtract(0, 1, 0));
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) {
            getUPlayer(e.getPlayer()).cancelDescendTimer();
            return;
        }
        
        UPlayer up = getUPlayer(e.getPlayer());
        
        up.setLocation(e.getPlayer().getLocation().subtract(0, 2, 0));
        up.createDescendTimer();
    }
    
    public static Model getBaseModel() {
        return instance.base;
    }
    
    public static Model getToolsModel() {
        return instance.tools;
    }
    
    public static Model getLightModel() {
        return instance.light;
    }
    
    public static boolean canPassThrough(Material type, byte data) {
        for (BlockData pass : instance.passThrough) {
            if (pass.getType() == type && (pass.getData() >= 0 ? pass.getData() == data : true))
                return true;
        }
        return false;
    }
    
    public static int getDescendSpeed() {
        return instance.descendSpeed;
    }
    
    public static FlyingCarpet getInstance() {
        return instance;
    }
    
}

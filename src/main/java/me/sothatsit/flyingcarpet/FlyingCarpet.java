package me.sothatsit.flyingcarpet;

import java.util.*;

import me.sothatsit.flyingcarpet.hooks.PluginHooks;
import me.sothatsit.flyingcarpet.hooks.RedProtectHook;
import me.sothatsit.flyingcarpet.hooks.WorldGuardHook;
import me.sothatsit.flyingcarpet.message.ConfigWrapper;
import me.sothatsit.flyingcarpet.message.Messages;

import me.sothatsit.flyingcarpet.model.BlockOffset;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class FlyingCarpet extends JavaPlugin implements Listener {

    private static FlyingCarpet instance;

    private FCConfig mainConfig;
    static List<PluginHooks> pluginHooks;
    private List<UPlayer> players = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        getCommand("flyingcarpet").setExecutor(new FCCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

        Messages.setConfig(new ConfigWrapper(this, "lang.yml"));

        pluginHooks = new ArrayList<>();
        setupHooks();

        mainConfig = new FCConfig();
        mainConfig.reloadConfiguration();
        info("FlyingCarpet Enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;

        for (UPlayer up : players) {
            up.setEnabled(false);
        }
    }

    public boolean isCarpetAllowed(Location loc) {
        for (PluginHooks plugin : pluginHooks) {
            if (!plugin.isCarpetAllowed(loc))
                return false;
        }
        return true;
    }

    private void setupHooks() {
        Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            pluginHooks.add(new WorldGuardHook());
            info("Hooked WorldGuard");
        }

        Plugin redProtect = getServer().getPluginManager().getPlugin("RedProtect");
        if (redProtect != null) {
            pluginHooks.add(new RedProtectHook());
            info("Hooked RedProtect");
        }
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
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
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
        List<Block> remove = new ArrayList<>();

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
        if (e.getCause() != DamageCause.SUFFOCATION)
            return;

        Entity entity = e.getEntity();
        Location location = entity.getLocation();

        for (UPlayer up : players) {
            if (!up.isCarpetBlock(location))
                continue;

            e.setCancelled(true);
            return;
        }

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            Location eyeLocation = livingEntity.getEyeLocation();

            for (UPlayer up : players) {
                if (!up.isCarpetBlock(eyeLocation))
                    continue;

                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getCause() != DamageCause.FALL || !(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();
        UPlayer up = getUPlayer(p);

        if (up.isEnabled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled() || BlockOffset.locEqual(e.getFrom(), e.getTo()))
            return;

        UPlayer up = getUPlayer(e.getPlayer());

        if (BlockOffset.locColumnEqual(e.getFrom(), e.getTo()) && Math.abs(e.getFrom().getY() - e.getTo().getY()) <= 2) {
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
        if (e.isCancelled() || BlockOffset.locEqual(e.getFrom(), e.getTo()))
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
            getUPlayer(e.getPlayer()).stopDescent();
            return;
        }

        UPlayer up = getUPlayer(e.getPlayer());

        up.setLocation(e.getPlayer().getLocation().subtract(0, 2, 0));
        up.startDescent();
    }

    public static FlyingCarpet getInstance() {
        return instance;
    }

    public static FCConfig getMainConfig() {
        return instance.mainConfig;
    }

    public static void info(String info) {
        instance.getLogger().info(info);
    }

    public static void warning(String warning) {
        instance.getLogger().warning(warning);
    }

    public static void severe(String severe) {
        instance.getLogger().severe(severe);
    }

    public static void sync(Runnable task) {
        Bukkit.getScheduler().runTask(instance, task);
    }

    public static void async(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, task);
    }
}

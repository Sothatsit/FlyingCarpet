package me.sothatsit.flyingcarpet;

import me.sothatsit.flyingcarpet.message.Messages;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FCCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0 && args[0].equalsIgnoreCase("worldguard")) {
            if (!sender.hasPermission("flyingcarpet.worldguard")) {
                Messages.get("error.no-permissions.worldguard").send(sender);
                return true;
            }

            if (FlyingCarpet.pluginHooks.stream().noneMatch(p -> p.hookName().equals("WorldGuard"))) {
                Messages.get("error.worldguard-not-hooked").send(sender);
                return true;
            }

            if (args.length == 1) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc worldguard <add:remove:list>").send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("add")) {
                if (args.length != 3) {
                    Messages.get("error.invalid-arguments").argument("%valid%", "/mc worldguard add <region>").send(sender);
                    return true;
                }

                FlyingCarpet.getMainConfig().addWorldguardBlacklistedRegion(args[2]);
                Messages.get("message.wg.add").argument("%region%", args[2]).send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                if (args.length != 3) {
                    Messages.get("error.invalid-arguments").argument("%valid%", "/mc worldguard remove <region>").send(sender);
                    return true;
                }

                FlyingCarpet.getMainConfig().removeWorldguardBlacklistedRegion(args[2]);
                Messages.get("message.wg.remove").argument("%region%", args[2]).send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("list")) {
                if (args.length != 2) {
                    Messages.get("error.invalid-arguments").argument("%valid%", "/mc worldguard list").send(sender);
                    return true;
                }

                Set<String> regions = FlyingCarpet.getMainConfig().getWorldguardBlacklistedRegions();

                if (regions.size() == 0) {
                    Messages.get("message.wg.list-none").send(sender);
                    return true;
                }

                Messages.get("message.wg.list-header").send(sender);

                for (String region : regions) {
                    Messages.get("message.wg.list-line").argument("%region%", region).send(sender);
                }

                return true;
            }

            Messages.get("error.invalid-arguments").argument("%valid%", "/mc worldguard <add:remove:list>").send(sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flyingcarpet.reload")) {
                Messages.get("error.no-permissions.reload").send(sender);
                return false;
            }

            FlyingCarpet.getMainConfig().reloadConfiguration();

            Messages.get("message.reload").send(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            Messages.get("error.must-be-player").send(sender);
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("flyingcarpet.use")) {
            Messages.get("error.no-permissions.carpet").send(sender);
            return true;
        }

        UPlayer up = FlyingCarpet.getInstance().getUPlayer(p);

        if (args.length == 0) {
            if (!up.isEnabled() && !FlyingCarpet.getInstance().isCarpetAllowed(p.getLocation())) {
                Messages.get("error.region-blocked").send(sender);
                return true;
            }

            up.setEnabled(!up.isEnabled());

            if (up.isEnabled()) {
                Messages.get("message.carpet-on").send(sender);
            } else {
                Messages.get("message.carpet-off").send(sender);
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (args.length != 1) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc on").send(sender);
                return true;
            }

            if (!FlyingCarpet.getInstance().isCarpetAllowed(p.getLocation())) {
                Messages.get("error.region-blocked").send(sender);
                return true;
            }

            up.setEnabled(true);

            Messages.get("message.carpet-on").send(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (args.length != 1) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc off").send(sender);
                return true;
            }

            up.setEnabled(false);

            Messages.get("message.carpet-off").send(sender);
            return true;
        }

        if (Stream.of(Material.values()).anyMatch(m->m.name().equalsIgnoreCase(args[0]))) {
            String mat = args[0];
            if (!mat.equalsIgnoreCase("glass") && !p.hasPermission("flyingcarpet.material." + mat)) {
                Messages.get("error.no-permissions.material").send(sender);
                return false;
            }

            if (args.length != 1) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc <material>").send(sender);
                return true;
            }

            up.setMaterial(mat);
            Messages.get("message.material").argument("%block%", mat).send(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("size")) {
            try {
                int size = Integer.parseInt(args[1]);
                if (size != 2 && !p.hasPermission("flyingcarpet.size." + size)) {
                    Messages.get("error.no-permissions.size").send(sender);
                    return false;
                }

                if (args.length != 2 || (size < 2 || size > 10)) {
                    Messages.get("error.invalid-arguments").argument("%valid%", "/mc size <number 2-10>").send(sender);
                    return true;
                }

                up.setSize(size);
                Messages.get("message.size").argument("%size%", String.valueOf(size)).send(sender);
            } catch (Exception ex) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc size <number 2-10>").send(sender);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("touch")) {
            if (!p.hasPermission("flyingcarpet.touch")) {
                Messages.get("error.no-permissions.touch").send(sender);
                return false;
            }

            if (args.length != 1 && args.length != 2) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc touch [on:off]").send(sender);
            }

            if (args.length == 1) {
                up.setTouch(!up.isTouch());
                if (up.isTouch())
                    Messages.get("message.touch-on").send(sender);
                else
                    Messages.get("message.touch-off").send(sender);

                return true;
            }

            if (args[1].equalsIgnoreCase("on")) {
                up.setTouch(true);

                Messages.get("message.touch-on").send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("off")) {
                up.setTouch(false);

                Messages.get("message.touch-off").send(sender);
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("tools")) {
            if (!p.hasPermission("flyingcarpet.tools")) {
                Messages.get("error.no-permissions.tools").send(sender);
                return false;
            }

            if (args.length != 1 && args.length != 2) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc tools [on:off]").send(sender);
                return true;
            }

            if (args.length == 1) {
                up.setTools(!up.isTools());

                if (up.isTools())
                    Messages.get("message.tools-on").send(sender);
                else
                    Messages.get("message.tools-off").send(sender);

                return true;
            }

            if (args[1].equalsIgnoreCase("on")) {
                up.setTools(true);

                Messages.get("message.tools-on").send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("off")) {
                up.setTools(false);

                Messages.get("message.tools-off").send(sender);
                return true;
            }

            Messages.get("error.invalid-arguments").argument("%valid%", "/mc tools [on:off]").send(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("light")) {
            if (!p.hasPermission("flyingcarpet.light")) {
                Messages.get("error.no-permissions.light").send(sender);
                return true;
            }

            if (args.length != 1 && args.length != 2) {
                Messages.get("error.invalid-arguments").argument("%valid%", "/mc light [on:off]").send(sender);
                return true;
            }

            if (args.length == 1) {
                up.setLight(!up.isLight());

                if (up.isLight())
                    Messages.get("message.light-on").send(sender);
                else
                    Messages.get("message.light-off").send(sender);

                return true;
            }

            if (args[1].equalsIgnoreCase("on")) {
                up.setLight(true);

                Messages.get("message.light-on").send(sender);
                return true;
            }

            if (args[1].equalsIgnoreCase("off")) {
                up.setLight(false);

                Messages.get("message.light-off").send(sender);
                return true;
            }

            Messages.get("error.invalid-arguments").argument("%valid%", "/mc light [on:off]").send(sender);
            return true;
        }

        String arguments = "on:off:tools:light:touch:size:<material>";

        if (FlyingCarpet.pluginHooks.stream().anyMatch(pl -> pl.hookName().equals("WorldGuard")) && p.hasPermission("flyingcarpet.worldguard")) {
            arguments += ":worldguard";
        }

        Messages.get("error.invalid-arguments").argument("%valid%", "/mc [" + arguments + "]").send(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> tab = new ArrayList<>();
        if (strings.length == 1) {
            if (commandSender.hasPermission("flyingcarpet.tools"))
                tab.add("tools");
            if (commandSender.hasPermission("flyingcarpet.light"))
                tab.add("light");
            if (commandSender.hasPermission("flyingcarpet.touch"))
                tab.add("touch");
            if (commandSender.hasPermission("flyingcarpet.reload"))
                tab.add("reload");
            tab.add("size");
            tab.addAll(Arrays.stream(Material.values()).filter(m->commandSender.hasPermission("flyingcarpet.material."+m.name().toLowerCase())).map(m->m.name().toLowerCase()).collect(Collectors.toList()));
        }

        return tab;
    }
}

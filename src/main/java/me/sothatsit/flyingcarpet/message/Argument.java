package me.sothatsit.flyingcarpet.message;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Argument {
    
    private String argument;
    private String value;
    
    public Argument(String argument, String value) {
        this.argument = argument;
        this.value = value;
    }
    
    public String getArgument() {
        return argument;
    }
    
    public String getValue() {
        return value;
    }
    
    public String replace(String str) {
        return str.replace(argument, value);
    }
    
    public static Argument sender(CommandSender sender) {
        return new Argument("%sender%", sender.getName());
    }
    
    public static Argument player(Player p) {
        return new Argument("%player%", p.getName());
    }
    
    public static Argument player(String name) {
        return new Argument("%player%", name);
    }
    
    public static Argument valid(String valid) {
        return new Argument("%valid%", valid);
    }
    
    public static Argument action(String action) {
        return new Argument("%action%", action);
    }
}

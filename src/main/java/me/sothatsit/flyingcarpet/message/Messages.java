package me.sothatsit.flyingcarpet.message;

import java.util.Calendar;

import org.bukkit.configuration.file.FileConfiguration;

public class Messages {
    
    private static ConfigWrapper config = null;
    
    public static Message get(String key) {
        if (config == null)
            return new Message(key);
        
        Message message = new Message(key);
        
        if (config.getConfig().isConfigurationSection(key))
            key += ".default";
        
        if (config.getConfig().isString(key)) {
            
            message = new Message(key, config.getConfig().getString(key));
        }
        else if (config.getConfig().isList(key)) {
            
            message = new Message(key, config.getConfig().getStringList(key));
        }
        
        return message;
    }
    
    public static String formatMilliseconds(long ms) {
        return get("format.time.milliseconds").argument("%milliseconds%", ms + "").getMessage();
    }
    
    public static String formatTime(long s) {
        long m = (long) Math.floor(s / 60d);
        long h = (long) Math.floor(m / 60d);
        long d = (long) Math.floor(h / 24d);
        long w = (long) Math.floor(d / 7d);
        
        s -= m * 60;
        m -= h * 60;
        h -= d * 24;
        
        Argument seconds = new Argument("%seconds%", get("format.time.seconds").argument("%seconds%", s + "").getMessage());
        Argument minutes = new Argument("%minutes%", get("format.time.minutes").argument("%minutes%", m + "").getMessage());
        Argument hours = new Argument("%hours%", get("format.time.hours").argument("%hours%", h + "").getMessage());
        Argument days = new Argument("%days%", get("format.time.days").argument("%days%", d + "").getMessage());
        
        if (m < 1)
            return get("format.time.less-1-min").arguments(seconds, minutes, hours, days).getMessage();
        
        if (h < 1)
            return get("format.time.less-1-hour").arguments(seconds, minutes, hours, days).getMessage();
        
        if (d < 1)
            return get("format.time.less-1-day").arguments(seconds, minutes, hours, days).getMessage();
        
        if (w < 1)
            return get("format.time.less-1-week").arguments(seconds, minutes, hours, days).getMessage();
        
        return get("format.time.more-1-week").arguments(seconds, minutes, hours, days).getMessage();
    }
    
    public static String formatDate(long time) {
        Calendar c = Calendar.getInstance();
        
        c.setTimeInMillis(time);
        
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        
        String dayStr = Messages.get("format.date.day.day-" + day).argument("%day%", day + "").getMessage();
        
        if (dayStr.isEmpty())
            dayStr = Messages.get("format.date.day").argument("%day%", day + "").getMessage();
        
        String monthStr = Messages.get("format.date.month.month-" + month).argument("%month%", month + "").getMessage();
        
        if (monthStr.isEmpty())
            monthStr = Messages.get("format.date.month").argument("%month%", month + "").getMessage();
        
        Argument minuteArg = new Argument("%minute%", minute + "");
        Argument hour24Arg = new Argument("%24hour%", hour + "");
        Argument hour12Arg = new Argument("%12hour%", (hour > 12 ? hour - 12 : hour) + "");
        
        String meridiemMore12 = Messages.get("format.date.meridiem.pm").getMessage();
        String meridiemLess12 = Messages.get("format.date.meridiem.am").getMessage();
        Argument meridiem = new Argument("%meridiem%", (hour > 12 ? meridiemMore12 : meridiemLess12));
        
        Argument dayArg = new Argument("%day%", dayStr);
        Argument monthArg = new Argument("%month%", monthStr);
        Argument yearArg = new Argument("%year%", year + "");
        
        return Messages.get("format.date").arguments(minuteArg, hour24Arg, hour12Arg, meridiem, dayArg, monthArg, yearArg).getMessage();
    }
    
    public static void setConfig(ConfigWrapper config) {
        Messages.config = config;
        
        reload();
    }
    
    public static void reload() {
        if (config == null)
            return;
        
        boolean changed = false;
        FileConfiguration defaults = config.getDefaultConfig();
        FileConfiguration conf = config.getConfig();
        
        for (String key : defaults.getKeys(true)) {
            if (!conf.isSet(key)) {
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
}

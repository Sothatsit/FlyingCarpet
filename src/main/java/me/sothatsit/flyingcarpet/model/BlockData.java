package me.sothatsit.flyingcarpet.model;

import me.sothatsit.flyingcarpet.FlyingCarpet;
import me.sothatsit.flyingcarpet.util.Checks;
import me.sothatsit.flyingcarpet.util.Clock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockData {
    
    public static final BlockData AIR = new BlockData(Material.AIR);

    public static final BlockData WATER = new BlockData(Material.WATER);
    public static final BlockData STATIONARY_WATER = new BlockData(Material.STATIONARY_WATER);

    public static final BlockData LAVA = new BlockData(Material.LAVA);
    public static final BlockData STATIONARY_LAVA = new BlockData(Material.STATIONARY_LAVA);
    
    private final Material type;
    private final byte data;
    
    public BlockData(Material type) {
        this(type, (byte) -1);
    }
    
    public BlockData(Material type, byte data) {
        Checks.ensureNonNull(type, "type");
        Checks.ensureTrue(data >= -1 && data <= 15, "data must be -1 (no data) or between 0 and 15 inclusive");

        this.type = type;
        this.data = data;
    }
    
    public Material getType() {
        return type;
    }

    public boolean hasData() {
        return data != -1;
    }

    public byte getData() {
        return (hasData() ? data : 0);
    }

    public boolean isAir() {
        return type == Material.AIR;
    }

    public BlockData withData(byte data) {
        return new BlockData(type, data);
    }

    @SuppressWarnings("deprecation")
    public void apply(Block b) {
        if (b.getType() == type && b.getData() == getData())
            return;

        b.setType(type, false);

        if(hasData()) {
            b.setData(data, false);
        }
    }

    public void addToSet(Set<BlockData> set) {
        set.add(this);

        if(hasData())
            return;

        for(byte data = 0; data < 16; ++data) {
            set.add(new BlockData(type, data));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BlockData))
            return false;

        BlockData other = (BlockData) obj;

        return type == other.type && data == other.data;
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ Byte.hashCode(data);
    }

    @Override
    public String toString() {
        String typeName = type.name().toLowerCase();

        return typeName + (hasData() ? ":" + data : "");
    }

    @SuppressWarnings("deprecation")
    public static BlockData fromBlock(Block block) {
        return new BlockData(block.getType(), block.getData());
    }

    public static class BlockDataParseException extends Exception {

        private BlockDataParseException(String reason) {
            super(reason);
        }

    }

    public static BlockData fromString(String string, AtomicBoolean requiresReformat) throws BlockDataParseException {
        int index = string.indexOf(':');

        if(index >= 0) {
            String typeName = string.substring(0, index);
            String dataString = string.substring(index + 1, string.length());

            return new BlockData(parseType(typeName, requiresReformat), parseData(dataString));
        }

        return new BlockData(parseType(string, requiresReformat));
    }

    @SuppressWarnings("deprecation")
    private static Material parseType(String typeName, AtomicBoolean requiresReformat) throws BlockDataParseException {
        try {
            int typeId = Integer.valueOf(typeName);

            requiresReformat.set(true);

            Material type = Material.getMaterial(typeId);

            if(type == null)
                throw new BlockDataParseException("unknown type " + typeName);

            return type;
        } catch(NumberFormatException e) {
            return Material.matchMaterial(typeName);
        }
    }

    private static byte parseData(String data) throws BlockDataParseException {
        try {
            int dataInt = Integer.valueOf(data);

            if(dataInt < 0 || dataInt >= 16)
                throw new BlockDataParseException("data of type must be between 0 and 15 inclusive");

            return (byte) dataInt;
        } catch(NumberFormatException e) {
            throw new BlockDataParseException("data of type must be an integer");
        }
    }

    public static BlockData fromSection(ConfigurationSection section, AtomicBoolean shouldSave) {
        AtomicBoolean modified = new AtomicBoolean(false);

        if(!section.isSet("block-type")) {
            FlyingCarpet.severe("Expected " + section.getCurrentPath() + " to have a block-type");
            return null;
        }

        String typeString = section.getString("block-type");
        BlockData type;

        try {
            type = fromString(typeString, modified);
        } catch (BlockDataParseException e) {
            FlyingCarpet.severe("Unable to parse type for " + section.getCurrentPath() + ", " + e.getMessage() +
                                " defaulting to glass");

            return new BlockData(Material.GLASS);
        }

        String dataString = null;

        if(section.isSet("block-data") && section.isInt("block-data")) {
            modified.set(true);

            if(type.hasData()) {
                FlyingCarpet.warning(section.getCurrentPath() + " has a block-type with data and a block-data value, " +
                                     "removing the block-data value");
            } else {
                try {
                    dataString = section.getString("block-data");
                    byte data = parseData(dataString);

                    // As we are not using this to test for block types, it makes more sense
                    // to default a block-data of 0 to no data as it functions the same for
                    // models and avoids appending an unnecessary ":0" to the type in the config.
                    if(data != 0) {
                        type = type.withData(data);
                    }
                } catch (BlockDataParseException e) {
                    FlyingCarpet.severe("Unable to parse data for " + section.getCurrentPath() + ", " + e.getMessage() +
                                        " defaulting to no data");
                }
            }
        }

        if(modified.get()) {
            shouldSave.set(true);

            section.set("block-type", type.toString());
            section.set("block-data", null);

            String from = typeString + (dataString == null ? "" : ":" + dataString);

            FlyingCarpet.info("1.13 Prep - " + from + " converted to " +
                              type.toString() + " for " + section.getCurrentPath() + " in the config.yml");
        }

        return type;
    }
    
}

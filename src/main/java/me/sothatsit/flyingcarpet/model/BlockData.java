package me.sothatsit.flyingcarpet.model;

import me.sothatsit.flyingcarpet.FlyingCarpet;
import me.sothatsit.flyingcarpet.util.Checks;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockData {
    
    public static final BlockData AIR = new BlockData(Material.AIR);

    public static final BlockData WATER = new BlockData(Material.WATER);
    @SuppressWarnings("deprecation")
	public static final BlockData STATIONARY_WATER = new BlockData(Material.LEGACY_STATIONARY_WATER);

    public static final BlockData LAVA = new BlockData(Material.LAVA);
    @SuppressWarnings("deprecation")
	public static final BlockData STATIONARY_LAVA = new BlockData(Material.LEGACY_STATIONARY_LAVA);
    
    private final Material type;
    
    public BlockData(Material type) {
        Checks.ensureNonNull(type, "type");

        this.type = type;
    }
    
    public Material getType() {
        return type;
    }

    public boolean isAir() {
        return type == Material.AIR;
    }

    @SuppressWarnings("deprecation")
    public void apply(Block b) {
        if (b.getType() == type)
            return;

        b.setType(type, false);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BlockData))
            return false;

        BlockData other = (BlockData) obj;

        return type == other.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type.name().toLowerCase();
    }

    public static BlockData fromBlock(Block block) {
        return new BlockData(block.getType());
    }

    public static class BlockDataParseException extends Exception {
		private static final long serialVersionUID = 1L;

		private BlockDataParseException(String reason) {
            super(reason);
        }

    }

    public static BlockData fromString(String string) throws BlockDataParseException {
        return new BlockData(parseType(string));
    }

    private static Material parseType(String typeName) throws BlockDataParseException {
        Material type = Material.matchMaterial(typeName);
        if(type == null)
            throw new BlockDataParseException("Could not find block type " + typeName);

        return type;
    }

    public static BlockData fromSection(ConfigurationSection section, AtomicBoolean shouldSave) {
        if(!section.isSet("block-type")) {
            FlyingCarpet.severe("Expected " + section.getCurrentPath() + " to have a block-type");
            return null;
        }

        String typeString = section.getString("block-type");
        BlockData type;

        try {
            type = fromString(typeString);
        } catch (BlockDataParseException e) {
            FlyingCarpet.severe("Unable to parse type for " + section.getCurrentPath() + ", " + e.getMessage() +
                                ", defaulting to glass");

            return new BlockData(Material.GLASS);
        }

        if(section.isSet("block-data")) {
            section.set("block-data", null);
            shouldSave.set(true);
        }

        return type;
    }
}

package me.sothatsit.flyingcarpet.model;

import me.sothatsit.flyingcarpet.util.Checks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class BlockOffset {
    
    public final int x;
    public final int y;
    public final int z;
    
    public BlockOffset(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBlock(World world) {
        return world.getBlockAt(x, y, z);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BlockOffset))
            return super.equals(obj);
        
        BlockOffset other = (BlockOffset) obj;
        
        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(x) ^ Integer.hashCode(y) ^ Integer.hashCode(z);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    public static BlockOffset fromString(String string) {
        String[] split = string.split(",");

        if(split.length != 3)
            return null;

        try {
            int x = Integer.valueOf(split[0].trim());
            int y = Integer.valueOf(split[1].trim());
            int z = Integer.valueOf(split[2].trim());

            return new BlockOffset(x, y, z);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static BlockOffset min(BlockOffset one, BlockOffset two) {
        Checks.ensureNonNull(one, "one");
        Checks.ensureNonNull(two, "two");

        int x = (one.x < two.x ? one.x : two.x);
        int y = (one.y < two.y ? one.y : two.y);
        int z = (one.z < two.z ? one.z : two.z);

        return new BlockOffset(x, y, z);
    }

    public static BlockOffset max(BlockOffset one, BlockOffset two) {
        Checks.ensureNonNull(one, "one");
        Checks.ensureNonNull(two, "two");

        int x = (one.x > two.x ? one.x : two.x);
        int y = (one.y > two.y ? one.y : two.y);
        int z = (one.z > two.z ? one.z : two.z);

        return new BlockOffset(x, y, z);
    }

    public static BlockOffset from(Location from, Location to) {
        return new BlockOffset(
                to.getBlockX() - from.getBlockX(),
                to.getBlockY() - from.getBlockY(),
                to.getBlockZ() - from.getBlockZ());
    }

    public static BlockOffset fromLocation(Location loc) {
        return new BlockOffset(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static BlockOffset fromBlock(Block loc) {
        return new BlockOffset(loc.getX(), loc.getY(), loc.getZ());
    }

    public static BlockOffset fromConfig(ConfigurationSection section) {
        if (!section.isInt("x") || !section.isInt("y") || !section.isInt("z"))
            return null;
        
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        
        return new BlockOffset(x, y, z);
    }

    public static boolean locEqual(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean locColumnEqual(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

}

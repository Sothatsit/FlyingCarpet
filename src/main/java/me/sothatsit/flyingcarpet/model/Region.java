package me.sothatsit.flyingcarpet.model;

import me.sothatsit.flyingcarpet.util.Checks;

import java.util.List;

public class Region {

    public final BlockOffset min;
    public final BlockOffset max;

    public Region(BlockOffset at) {
        this(at, at);
    }

    public Region(BlockOffset from, BlockOffset to) {
        this(from, to, false);
    }

    private Region(BlockOffset from, BlockOffset to, boolean isMinMax) {
        Checks.ensureNonNull(from, "from");
        Checks.ensureNonNull(to, "to");

        if (isMinMax) {
            min = from;
            max = to;
        } else {
            min = BlockOffset.min(from, to);
            max = BlockOffset.max(from, to);
        }
    }

    private static int abs(int val) {
        return (val < 0 ? val * -1 : val);
    }

    public static Region combine(List<Region> regions) {
        Checks.ensureNonNull(regions, "regions");
        Checks.ensureTrue(regions.size() > 0, "regions cannot be empty");

        Region first = regions.get(0);

        int minx = first.min.x;
        int miny = first.min.y;
        int minz = first.min.z;

        int maxx = first.max.x;
        int maxy = first.max.y;
        int maxz = first.max.z;

        for (int index = 1; index < regions.size(); ++index) {
            Region region = regions.get(index);

            if (region.min.x < minx)
                minx = region.min.x;
            if (region.min.y < miny)
                miny = region.min.y;
            if (region.min.z < minz)
                minz = region.min.z;

            if (region.max.x > maxx)
                maxx = region.max.x;
            if (region.max.y > maxy)
                maxy = region.max.y;
            if (region.max.z > maxz)
                maxz = region.max.z;
        }

        BlockOffset min = new BlockOffset(minx, miny, minz);
        BlockOffset max = new BlockOffset(maxx, maxy, maxz);

        return new Region(min, max, true);
    }

    public boolean inBounds(BlockOffset loc) {
        return loc.x >= min.x && loc.x <= max.x
                && loc.y >= min.y && loc.y <= max.y
                && loc.z >= min.z && loc.z <= max.z;
    }

    public int getVolume() {
        return abs(max.x - min.x) * abs(max.y - min.y) * abs(max.z - min.z);
    }

    @Override
    public String toString() {
        if (min.equals(max))
            return "at (" + min + ")";

        return "from (" + min + ") to (" + max + ")";
    }

}

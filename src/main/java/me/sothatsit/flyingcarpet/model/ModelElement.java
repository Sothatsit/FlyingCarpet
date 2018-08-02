package me.sothatsit.flyingcarpet.model;

import me.sothatsit.flyingcarpet.util.Checks;

public class ModelElement {
    
    public final BlockData blockData;
    public final Region region;
    
    public ModelElement(BlockData blockData, Region region) {
        Checks.ensureNonNull(blockData, "blockData");
        Checks.ensureNonNull(region, "region");

        this.blockData = blockData;
        this.region = region;
    }

    public boolean inBounds(BlockOffset offset) {
        return region.inBounds(offset);
    }

    @Override
    public String toString() {
        return blockData + " " + region;
    }

}

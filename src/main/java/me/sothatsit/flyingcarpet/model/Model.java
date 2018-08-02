package me.sothatsit.flyingcarpet.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import me.sothatsit.flyingcarpet.FlyingCarpet;
import me.sothatsit.flyingcarpet.util.Checks;

import org.bukkit.configuration.ConfigurationSection;

public class Model {
    
    private final ModelElement[] elements;
    public final Region region;

    public Model(List<ModelElement> elements) {
        Checks.ensureNonNull(elements, "elements");

        this.elements = elements.toArray(new ModelElement[elements.size()]);

        List<Region> regions = new ArrayList<>(elements.size());

        for(ModelElement element : elements) {
            regions.add(element.region);
        }

        this.region = Region.combine(regions);
    }
    
    public BlockData getBlockData(BlockOffset offset) {
        if(!region.inBounds(offset))
            return BlockData.AIR;

        for(int index = elements.length - 1; index >= 0; --index) {
            if(elements[index].inBounds(offset))
                return elements[index].blockData;
        }
        
        return BlockData.AIR;
    }

    public static BlockData getBlockData(List<Model> models, BlockOffset offset) {
        for(int index = models.size() - 1; index >= 0; --index) {
            BlockData data = models.get(index).getBlockData(offset);

            if(!data.isAir())
                return data;
        }

        return BlockData.AIR;
    }

    public static Model fromConfig(ConfigurationSection config, AtomicBoolean requiresSave) {
        List<ModelElement> elements = new ArrayList<>();
        
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key))
                continue;
            
            ConfigurationSection section = config.getConfigurationSection(key);
            ModelElement element = loadModelElement(section, requiresSave);

            if(element == null)
                continue;

            elements.add(element);
        }
        
        return new Model(elements);
    }

    private static void migrateModelElement(ConfigurationSection section) {
        String modelType = section.getString("model-type");

        if(modelType.equalsIgnoreCase("block")) {
            if(!section.isConfigurationSection("offset")) {
                FlyingCarpet.warning("Unable to find offset while migrating " + section.getCurrentPath());
                return;
            }

            BlockOffset offset = BlockOffset.fromConfig(section.getConfigurationSection("offset"));

            if (offset == null) {
                FlyingCarpet.warning("Invalid offset while migrating " + section.getCurrentPath());
                return;
            }

            section.set("model-type", null);
            section.set("offset", null);
            section.set("at", offset.toString());
            return;
        }

        if(modelType.equalsIgnoreCase("cuboid")) {
            if (!section.isConfigurationSection("from")) {
                FlyingCarpet.warning("Unable to find from while migrating " + section.getCurrentPath());
                return;
            }

            if (!section.isConfigurationSection("to")) {
                FlyingCarpet.warning("Unable to find to while migrating " + section.getCurrentPath());
                return;
            }

            BlockOffset from = BlockOffset.fromConfig(section.getConfigurationSection("from"));
            BlockOffset to = BlockOffset.fromConfig(section.getConfigurationSection("to"));

            if (from == null || to == null) {
                FlyingCarpet.warning("Invalid region while migrating " + section.getCurrentPath());
                return;
            }

            section.set("model-type", null);
            section.set("from", from.toString());
            section.set("to", to.toString());
            return;
        }

        FlyingCarpet.severe("Unknown model-type " + modelType + " while migrating " + section.getCurrentPath());
    }

    public static ModelElement loadModelElement(ConfigurationSection section, AtomicBoolean requiresSave) {
        if(section.isSet("model-type")) {
            migrateModelElement(section);
            requiresSave.set(true);

            FlyingCarpet.info("Migrated old model format for " + section.getCurrentPath());
        }

        BlockData data = BlockData.fromSection(section, requiresSave);

        if(data == null)
            return null;

        if(section.isSet("at")) {
            BlockOffset at = BlockOffset.fromString(section.getString("at"));

            if(at == null) {
                FlyingCarpet.severe("Unable to parse location for " + section.getCurrentPath() + ", invalid format");
                return null;
            }

            return new ModelElement(data, new Region(at));
        }

        if(section.isSet("from") && section.isSet("to")) {
            BlockOffset from = BlockOffset.fromString(section.getString("from"));
            BlockOffset to = BlockOffset.fromString(section.getString("to"));

            if (from == null || to == null) {
                FlyingCarpet.severe("Unable to parse region for " + section.getCurrentPath() + ", invalid format");
                return null;
            }

            return new ModelElement(data, new Region(from, to));
        }

        FlyingCarpet.getInstance().getLogger().warning("Invalid model element " + section.getCurrentPath()
                                                       + ", expected to find an at location or a from and to location");

        return null;
    }

}

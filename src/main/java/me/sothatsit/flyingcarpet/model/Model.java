package me.sothatsit.flyingcarpet.model;

import java.util.ArrayList;
import java.util.List;

import me.sothatsit.flyingcarpet.FlyingCarpet;
import me.sothatsit.flyingcarpet.util.BlockData;
import me.sothatsit.flyingcarpet.util.Region;
import me.sothatsit.flyingcarpet.util.Vector3I;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Model {
    
    private List<ModelElement> elements;
    
    public Model(List<ModelElement> elements) {
        this.elements = elements;
    }
    
    public List<ModelElement> getElements() {
        return elements;
    }
    
    public Region getRegion() {
        Region[] regions = new Region[elements.size()];
        
        for (int i = 0; i < regions.length; i++) {
            regions[i] = elements.get(i).getRegion();
        }
        
        return Region.combine(regions);
    }
    
    public BlockData getBlockData(Vector3I offset) {
        BlockData data = BlockData.AIR;
        
        for (ModelElement element : elements) {
            if (element.inBounds(offset))
                data = element.getBlockData();
        }
        
        return data;
    }
    
    public static Vector3I getOffset(Location centre, Location loc) {
        return new Vector3I(loc.getBlockX() - centre.getBlockX(), loc.getBlockY() - centre.getBlockY(), loc.getBlockZ() - centre.getBlockZ());
    }
    
    public static Model fromConfig(ConfigurationSection sec) {
        List<ModelElement> elements = new ArrayList<>();
        
        for (String key : sec.getKeys(false)) {
            if (!sec.isConfigurationSection(key))
                continue;
            
            ConfigurationSection elementSec = sec.getConfigurationSection(key);
            
            if (!elementSec.isString("model-type")) {
                FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"model-type\" not set or invalid");
                continue;
            }
            
            BlockData data = BlockData.fromSection(elementSec);
            
            if (data == null) {
                FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > block data not set or invalid");
                continue;
            }
            
            String modelType = elementSec.getString("model-type");
            
            if (modelType.equalsIgnoreCase("block")) {
                if (!elementSec.isConfigurationSection("offset")) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"offset\" not set");
                    continue;
                }
                
                Vector3I offset = Vector3I.fromConfig(elementSec.getConfigurationSection("offset"));
                
                if (offset == null) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"offset\" invalid");
                    continue;
                }
                
                BlockElement element = new BlockElement(data, offset);
                
                elements.add(element);
                continue;
            }
            
            if (modelType.equalsIgnoreCase("cuboid")) {
                if (!elementSec.isConfigurationSection("from")) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"from\" not set");
                    continue;
                }
                
                if (!elementSec.isConfigurationSection("to")) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"to\" not set");
                    continue;
                }
                
                Vector3I from = Vector3I.fromConfig(elementSec.getConfigurationSection("from"));
                
                if (from == null) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"from\" invalid");
                    continue;
                }
                
                Vector3I to = Vector3I.fromConfig(elementSec.getConfigurationSection("to"));
                
                if (to == null) {
                    FlyingCarpet.getInstance().getLogger().warning("Invalid model element for model \"" + sec.getName() + "\" > \"to\" invalid");
                    continue;
                }
                
                CuboidElement element = new CuboidElement(data, from, to);
                
                elements.add(element);
                continue;
            }
            
            FlyingCarpet.getInstance().getLogger()
                    .warning("Invalid model element for model \"" + sec.getName() + "\" > invalid model type, valid are \"block\" and \"cuboid\"");
        }
        
        return new Model(elements);
    }
}

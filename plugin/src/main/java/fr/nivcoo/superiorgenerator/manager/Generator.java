package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.config.Pair;
import org.bukkit.Material;

import java.util.HashMap;

public class Generator implements AGenerator {

    String id;
    String category;
    HashMap<Pair<Material, Byte>, Double> blocks;

    public Generator(String id, String category, HashMap<Pair<Material, Byte>, Double> blocks) {
        this.id = id;
        this.category = category;
        this.blocks = blocks;
    }

    public String getID() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public HashMap<Pair<Material, Byte>, Double> getBlocks() {
        return blocks;
    }


}

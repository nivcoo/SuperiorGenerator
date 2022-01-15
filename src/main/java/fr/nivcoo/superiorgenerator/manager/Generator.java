package fr.nivcoo.superiorgenerator.manager;

import org.bukkit.Material;

import java.util.HashMap;

public class Generator {

    String id;
    String category;
    HashMap<Material, Double> blocks;

    public Generator(String id, String category, HashMap<Material, Double> blocks) {
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

    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }


}

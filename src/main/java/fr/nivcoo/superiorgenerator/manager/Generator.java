package fr.nivcoo.superiorgenerator.manager;

import org.bukkit.Material;

import java.util.HashMap;

public class Generator {

    String id;
    HashMap<Material, Double> blocks;

    public Generator(String id, HashMap<Material, Double> blocks) {
        this.id = id;
        this.blocks = blocks;
    }

    public String getID() {
        return id;
    }

    public HashMap<Material, Double> getBlocks() {
        return blocks;
    }


}

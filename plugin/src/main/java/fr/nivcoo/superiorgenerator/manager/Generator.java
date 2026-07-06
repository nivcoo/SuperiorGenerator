package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.GeneratorBlock;

import java.util.List;

public class Generator implements AGenerator {

    private final String id;
    private final String category;
    private final List<GeneratorBlock> blocks;

    public Generator(String id, String category, List<GeneratorBlock> blocks) {
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

    public List<GeneratorBlock> getBlocks() {
        return blocks;
    }
}

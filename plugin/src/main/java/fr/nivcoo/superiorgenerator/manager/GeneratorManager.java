package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.config.GeneratorsConfig;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.AGeneratorManager;
import fr.nivcoo.superiorgeneratorapi.manager.GeneratorBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratorManager implements AGeneratorManager {

    private static final GeneratorBlock FALLBACK = new GeneratorBlock(Material.COBBLESTONE, 100.0D);

    private final GeneratorsConfig config;

    private List<AGenerator> generatorsList;
    private AGenerator defaultGenerator;

    public GeneratorManager() {
        SuperiorGenerator superiorGenerator = SuperiorGenerator.get();
        this.config = superiorGenerator.generators();
        loadGenerators();
        saveDefaultGenerator();
    }

    public void saveDefaultGenerator() {
        defaultGenerator = getGeneratorByID("default");
        if (defaultGenerator == null) {
            defaultGenerator = new Generator("default", "1", List.of(FALLBACK));
            generatorsList.add(defaultGenerator);
            Bukkit.getLogger().warning("[SuperiorGenerator] Missing default generator in generators.yml, using COBBLESTONE fallback.");
        }
    }

    public AGenerator getDefaultGenerator() {
        return defaultGenerator;
    }

    void loadGenerators() {
        generatorsList = new ArrayList<>();

        for (var entry : config.generators.entrySet()) {
            String id = entry.getKey();
            GeneratorsConfig.GeneratorConfig generatorConfig = entry.getValue();
            List<GeneratorBlock> blocks = new ArrayList<>();

            for (String blockString : generatorConfig.blocks) {
                String[] split = blockString.split(":", 2);
                if (split.length < 2) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] Invalid block entry: " + blockString);
                    continue;
                }

                String materialString = split[0];
                if (materialString.contains("!")) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] Legacy block data is no longer supported: " + blockString);
                    continue;
                }

                Material material;
                try {
                    material = Material.valueOf(materialString);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] The material '" + materialString + "' doesn't exist, please check your config!");
                    material = Material.COBBLESTONE;
                }

                double weight;
                try {
                    weight = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] Invalid weight for " + material + ": " + split[1]);
                    continue;
                }

                blocks.add(new GeneratorBlock(material, weight));
            }

            generatorsList.add(new Generator(id, generatorConfig.category, blocks));
        }
    }

    public AGenerator getGeneratorByID(String id) {
        return generatorsList.stream()
                .filter(generator -> generator.getID().equalsIgnoreCase(id))
                .findAny()
                .orElse(null);
    }

    public GeneratorBlock getRandomBlock(AGenerator generator) {
        List<GeneratorBlock> blocks = generator.getBlocks();
        if (blocks.isEmpty()) return FALLBACK;

        double total = 0.0D;
        for (GeneratorBlock block : blocks) total += Math.max(0D, block.weight());
        if (total <= 0.0D) return FALLBACK;

        double r = ThreadLocalRandom.current().nextDouble(total);
        double acc = 0.0D;

        for (GeneratorBlock block : blocks) {
            acc += Math.max(0D, block.weight());
            if (r < acc) return block;
        }

        return FALLBACK;
    }

    public List<AGenerator> getAllGenerators() {
        return generatorsList;
    }
}

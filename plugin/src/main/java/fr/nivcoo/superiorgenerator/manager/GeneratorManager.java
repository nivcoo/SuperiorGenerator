package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.AGeneratorManager;
import fr.nivcoo.utilsz.config.Config;
import fr.nivcoo.utilsz.config.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratorManager implements AGeneratorManager {

    private final Config config;

    private List<AGenerator> generatorsList;
    private AGenerator defaultGenerator;

    private static final Pair<Material, Byte> FALLBACK = new Pair<>(Material.COBBLESTONE, null);

    public GeneratorManager() {
        SuperiorGenerator superiorGenerator = SuperiorGenerator.get();
        this.config = superiorGenerator.getConfiguration();
        loadGenerators();
        saveDefaultGenerator();
    }

    public void saveDefaultGenerator() {
        defaultGenerator = getGeneratorByID("default");
    }

    public AGenerator getDefaultGenerator() {
        return defaultGenerator;
    }

    void loadGenerators() {
        generatorsList = new ArrayList<>();
        List<String> generators = config.getKeys("generators");

        for (String ID : generators) {
            String path = "generators." + ID + ".";
            String category = config.getString(path + "category");

            List<String> blocksString = config.getStringList(path + "blocks");
            LinkedHashMap<Pair<Material, Byte>, Double> blocks = new LinkedHashMap<>();

            for (String blockString : blocksString) {
                String[] split = blockString.split(":");
                if (split.length < 2) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] Invalid block entry: " + blockString);
                    continue;
                }

                String materialString = split[0];
                String[] splitData = materialString.split("!");
                materialString = splitData[0];
                Byte data = (splitData.length > 1) ? Byte.parseByte(splitData[1]) : null;

                Material material;
                try {
                    material = Material.valueOf(materialString);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] The material '" + materialString + "' doesn't exist, please check your config!");
                    material = Material.COBBLESTONE;
                    data = null;
                }

                double weight;
                try {
                    weight = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] Invalid weight for " + material + ": " + split[1]);
                    continue;
                }

                blocks.put(new Pair<>(material, data), weight);
            }

            generatorsList.add(new Generator(ID, category, blocks));
        }
    }

    public AGenerator getGeneratorByID(String ID) {
        return generatorsList.stream()
                .filter(generator -> generator.getID().equalsIgnoreCase(ID))
                .findAny()
                .orElse(null);
    }

    public Pair<Material, Byte> getRandomBlock(AGenerator generator) {
        Map<Pair<Material, Byte>, Double> map = generator.getBlocks();
        if (map.isEmpty()) return FALLBACK;

        double total = 0.0D;
        for (double w : map.values()) total += Math.max(0D, w);
        if (total <= 0.0D) return FALLBACK;

        double r = ThreadLocalRandom.current().nextDouble(total);
        double acc = 0.0D;

        for (Map.Entry<Pair<Material, Byte>, Double> entry : map.entrySet()) {
            acc += Math.max(0D, entry.getValue());
            if (r < acc) return entry.getKey();
        }

        return FALLBACK;
    }

    public List<AGenerator> getAllGenerators() {
        return generatorsList;
    }
}

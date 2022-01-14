package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Material;

import java.util.*;

public class GeneratorManager {


    private SuperiorGenerator superiorGenerator;
    private Config config;

    private List<Generator> generatorsList;

    public GeneratorManager() {
        superiorGenerator = SuperiorGenerator.get();

        config = superiorGenerator.getConfiguration();

        loadGenerators();

    }


    void loadGenerators() {
        generatorsList = new ArrayList<>();
        List<String> generators = config.getKeys("generators");

        for (String ID : generators) {

            String path = "generators." + ID + ".";

            List<String> blocksString = config.getStringList(path + "blocks");
            HashMap<Material, Double> blocks = new HashMap<>();
            for (String blockString : blocksString) {
                String[] split = blockString.split(":");
                blocks.put(Material.valueOf(split[0]), Double.valueOf(split[1]));
            }

            generatorsList.add(new Generator(ID, blocks));

        }
    }

    public Generator getGeneratorByID(String ID) {

        return generatorsList.stream().filter(generator -> generator.getID().equals(ID))
                .findAny().orElse(null);
    }

    public Material getRandomBlock(Generator generator) {
        Random random = new Random();
        double d = random.nextDouble() * 100.0D;
        for (Map.Entry<Material, Double> block : generator.getBlocks().entrySet()) {
            if ((d -= block.getValue()) < 0.0D)
                return block.getKey();
        }
        return Material.COBBLESTONE;
    }

    public List<Generator> getAllGenerators() {
        return generatorsList;
    }
}

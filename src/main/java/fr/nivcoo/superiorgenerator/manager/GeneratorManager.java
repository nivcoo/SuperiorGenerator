package fr.nivcoo.superiorgenerator.manager;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.utilsz.config.Config;
import fr.nivcoo.utilsz.config.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;

public class GeneratorManager {


    private SuperiorGenerator superiorGenerator;
    private Config config;

    private List<Generator> generatorsList;
    private Generator defaultGenerator;

    public GeneratorManager() {
        superiorGenerator = SuperiorGenerator.get();

        config = superiorGenerator.getConfiguration();

        loadGenerators();
        saveDefaultGenerator();

    }

    public void saveDefaultGenerator() {
        defaultGenerator = getGeneratorByID("default");
    }

    public Generator getDefaultGenerator() {
        return defaultGenerator;
    }

    void loadGenerators() {
        generatorsList = new ArrayList<>();
        List<String> generators = config.getKeys("generators");

        for (String ID : generators) {

            String path = "generators." + ID + ".";
            String category = config.getString(path + "category");

            List<String> blocksString = config.getStringList(path + "blocks");
            HashMap<Pair<Material, Byte>, Double> blocks = new HashMap<>();
            for (String blockString : blocksString) {
                String[] split = blockString.split(":");
                String materialString = split[0];

                String[] splitData = materialString.split("!");
                Byte data = null;
                if(splitData.length > 1) {
                    data = Byte.parseByte(splitData[1]);
                    materialString = splitData[0];
                }
                Material material;
                try {
                    material = Material.valueOf(materialString);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[SuperiorGenerator] The material : " + materialString + " doesn't exist, please check your config !");
                    material = Material.COBBLESTONE;
                }



                blocks.put(new Pair<>(material, data), Double.valueOf(split[1]));
            }

            generatorsList.add(new Generator(ID, category, blocks));

        }
    }

    public Generator getGeneratorByID(String ID) {

        return generatorsList.stream().filter(generator -> generator.getID().equals(ID))
                .findAny().orElse(null);
    }

    public Pair<Material, Byte> getRandomBlock(Generator generator) {
        Random random = new Random();
        double d = random.nextDouble() * 100.0D;
        for (Map.Entry<Pair<Material, Byte>, Double> block : generator.getBlocks().entrySet()) {
            if ((d -= block.getValue()) < 0.0D) {
                return block.getKey();
            }
        }
        return new Pair<>(Material.COBBLESTONE, null);
    }

    public List<Generator> getAllGenerators() {
        return generatorsList;
    }
}

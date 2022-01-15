package fr.nivcoo.superiorgenerator.cache;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.Generator;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.utils.Database;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CacheManager implements Listener {

    private SuperiorGenerator superiorGenerator;
    private Config config;
    private Database database;
    private GeneratorManager generatorManager;

    HashMap<UUID, List<Generator>> unlockedGenerators;
    HashMap<UUID, Generator> activeGenerators;


    public CacheManager() {
        superiorGenerator = SuperiorGenerator.get();

        config = superiorGenerator.getConfiguration();
        database = superiorGenerator.getDatabase();
        generatorManager = superiorGenerator.getGeneratorManager();
        unlockedGenerators = new HashMap<>();
        activeGenerators = new HashMap<>();
        updateAllConnectedPlayers();
    }

    public void updateAllConnectedPlayers() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            getCurrentIslandGenerator(p);
        }
    }


    void updateUnlockedGenerator(UUID islandUUID) {

        List<String> unlockedGeneratorsID = database.getAllUnlockedIslandGeneratorID(islandUUID);
        List<Generator> generators = new ArrayList<>();

        for (String unlockedGeneratorID : unlockedGeneratorsID) {
            Generator generator = generatorManager.getGeneratorByID(unlockedGeneratorID);
            if (generator != null)
                generators.add(generator);
        }

        unlockedGenerators.put(islandUUID, generators);

    }

    public Generator getCurrentIslandGenerator(Player p) {
        return getCurrentIslandGenerator(SuperiorSkyblock2.getIslandUUIDByMember(p));
    }

    public Generator getCurrentIslandGenerator(UUID islandUUID) {
        if (islandUUID == null)
            return generatorManager.getGeneratorByID("default");
        Generator generator = activeGenerators.get(islandUUID);
        if (generator != null)
            return generator;
        else {
            String generatorUUID = database.getCurrentIslandGeneratorID(islandUUID);
            Generator activeGenerator = generatorManager.getGeneratorByID(generatorUUID);
            if (activeGenerator == null)
                activeGenerator = generatorManager.getGeneratorByID("default");
            activeGenerators.put(islandUUID, activeGenerator);
            updateUnlockedGenerator(islandUUID);
            return activeGenerator;
        }
    }

    public boolean selectIslandGenerator(UUID islandUUID, Generator generator) {
        Generator gen = activeGenerators.get(islandUUID);
        if (gen == generator)
            return false;
        activeGenerators.put(islandUUID, generator);
        database.updateActiveGen(islandUUID, generator.getID());
        return true;
    }

    public boolean unlockGenerator(UUID islandUUID, Generator generator) {
        activeGenerators.put(islandUUID, generator);

        List<Generator> unlockedGenerator = unlockedGenerators.get(islandUUID);
        if (unlockedGenerator == null)
            unlockedGenerator = new ArrayList<>();
        if (unlockedGenerator.contains(generator) || generator == null || generator.getID().equals("default"))
            return false;
        unlockedGenerator.add(generator);

        unlockedGenerators.put(islandUUID, unlockedGenerator);
        if (!generator.getID().equals("default"))
            database.addOrEditUnlockedGenerator(islandUUID, generator.getID());
        return true;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(p);

        getCurrentIslandGenerator(islandUUID);

    }

    public boolean getIfUnlocked(UUID islandUUID, Generator generator) {
        List<Generator> generators = unlockedGenerators.get(islandUUID);
        if (generators == null || generator == null)
            return false;

        return generators.contains(generator) || generator.getID().equals("default");
    }

    public List<Generator> getAllUnlockedGeneratorsOfIsland(UUID islandUUID) {
        List<Generator> generators = unlockedGenerators.get(islandUUID);
        if (generators == null)
            generators = new ArrayList<>();
        return generators;
    }
}

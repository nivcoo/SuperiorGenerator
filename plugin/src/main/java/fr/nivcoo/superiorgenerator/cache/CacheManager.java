package fr.nivcoo.superiorgenerator.cache;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.utils.Database;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.redis.RedisMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CacheManager implements Listener {

    private SuperiorGenerator superiorGenerator;
    private Database database;
    private GeneratorManager generatorManager;

    HashMap<UUID, List<AGenerator>> unlockedGenerators;
    HashMap<UUID, AGenerator> activeGenerators;


    public CacheManager() {
        superiorGenerator = SuperiorGenerator.get();
        database = superiorGenerator.getDatabase();
        generatorManager = superiorGenerator.getGeneratorManager();
        unlockedGenerators = new HashMap<>();
        activeGenerators = new HashMap<>();
        updateAllConnectedPlayers();
    }

    public void updateAllConnectedPlayers() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            getOrUpdateCurrentIslandGenerator(p);
        }
    }


    void updateUnlockedGenerator(UUID islandUUID) {

        List<String> unlockedGeneratorsID = database.getAllUnlockedIslandGeneratorID(islandUUID);
        List<AGenerator> generators = new ArrayList<>();

        for (String unlockedGeneratorID : unlockedGeneratorsID) {
            AGenerator generator = generatorManager.getGeneratorByID(unlockedGeneratorID);
            if (generator != null) generators.add(generator);
        }

        unlockedGenerators.put(islandUUID, generators);

    }

    public void forceSelectGenerator(UUID islandUUID, AGenerator generator) {
        if (generator == null) return;
        if (!activeGenerators.containsKey(islandUUID)) return;
        activeGenerators.put(islandUUID, generator);
    }

    public void forceUnlockGenerator(UUID islandUUID, AGenerator generator) {
        if (generator == null) return;
        if (!unlockedGenerators.containsKey(islandUUID)) return;

        List<AGenerator> unlocked = unlockedGenerators.get(islandUUID);

        if (!unlocked.contains(generator)) {
            unlocked.add(generator);
        }
    }

    public AGenerator getOrUpdateCurrentIslandGenerator(Player p) {
        return getOrUpdateCurrentIslandGenerator(p, false);
    }

    private AGenerator getOrUpdateCurrentIslandGenerator(Player p, boolean forceUpdate) {
        return getOrUpdateCurrentIslandGenerator(SuperiorSkyblock2.getIslandUUIDByMember(p), forceUpdate);
    }

    public AGenerator getOrUpdateCurrentIslandGenerator(UUID islandUUID) {
        return getOrUpdateCurrentIslandGenerator(islandUUID, false);
    }


    public AGenerator getOrUpdateCurrentIslandGenerator(UUID islandUUID, boolean forceUpdate) {
        if (islandUUID == null) return generatorManager.getDefaultGenerator();

        AGenerator generator = activeGenerators.get(islandUUID);
        boolean hasUnlocked = unlockedGenerators.containsKey(islandUUID);

        if (generator != null && !forceUpdate && hasUnlocked) return generator;

        String generatorUUID = database.getCurrentIslandGeneratorID(islandUUID);
        AGenerator activeGenerator = generatorManager.getGeneratorByID(generatorUUID);
        if (activeGenerator == null) activeGenerator = generatorManager.getDefaultGenerator();

        activeGenerators.put(islandUUID, activeGenerator);

        if (!hasUnlocked) updateUnlockedGenerator(islandUUID);

        return activeGenerator;
    }


    public boolean selectIslandGenerator(UUID islandUUID, AGenerator generator) {
        AGenerator gen = activeGenerators.get(islandUUID);
        if (gen == generator) return false;
        activeGenerators.put(islandUUID, generator);
        database.updateActiveGen(islandUUID, generator.getID());
        if (superiorGenerator.isRedisEnabled()) {
            superiorGenerator.getRedisManager().publish(
                    "superiorgenerator-update",
                    new RedisMessage("select")
                            .add("islandUUID", islandUUID)
                            .add("generatorID", generator.getID())
                            .toJson()
            );
        }
        return true;
    }

    public boolean unlockGenerator(UUID islandUUID, AGenerator generator) {
        if (generator == null || generator.getID().equals("default")) return false;

        List<AGenerator> unlockedGenerator = unlockedGenerators.get(islandUUID);
        if (unlockedGenerator == null) unlockedGenerator = new ArrayList<>();

        if (unlockedGenerator.contains(generator)) return false;

        unlockedGenerator.add(generator);
        unlockedGenerators.put(islandUUID, unlockedGenerator);

        database.addOrEditUnlockedGenerator(islandUUID, generator.getID());
        if (superiorGenerator.isRedisEnabled()) {
            superiorGenerator.getRedisManager().publish(
                    "superiorgenerator-update",
                    new RedisMessage("unlock")
                            .add("islandUUID", islandUUID)
                            .add("generatorID", generator.getID())
                            .toJson()
            );
        }
        return true;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        getOrUpdateCurrentIslandGenerator(p);

    }

    public boolean isAlreadyUnlocked(UUID islandUUID, AGenerator generator) {
        List<AGenerator> generators = unlockedGenerators.get(islandUUID);
        if (generators == null || generator == null) return false;

        return generators.contains(generator) || generator.getID().equals("default");
    }

    public List<AGenerator> getAllUnlockedGeneratorsOfIsland(UUID islandUUID) {
        List<AGenerator> generators = unlockedGenerators.get(islandUUID);
        if (generators == null) generators = new ArrayList<>();
        AGenerator defaultGenerator = generatorManager.getDefaultGenerator();
        if (!generators.contains(defaultGenerator)) generators.add(defaultGenerator);
        return generators;
    }


    public int getUnlockedCategoriesNumber(UUID islandUUID, String category) {

        List<AGenerator> generators = getAllUnlockedGeneratorsOfIsland(islandUUID).stream().filter(generator -> generator.getCategory().equals(category)).collect(Collectors.toList());
        return generators.size();
    }
}

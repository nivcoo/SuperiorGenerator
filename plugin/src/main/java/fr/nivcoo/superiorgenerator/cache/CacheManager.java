package fr.nivcoo.superiorgenerator.cache;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.messaging.action.SelectAction;
import fr.nivcoo.superiorgenerator.messaging.action.UnlockAction;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.superiorgenerator.storage.Database;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheManager implements Listener {

    private SuperiorGenerator superiorGenerator;
    private Database database;
    private GeneratorManager generatorManager;

    HashMap<UUID, List<AGenerator>> unlockedGenerators;
    HashMap<UUID, AGenerator> activeGenerators;
    HashMap<UUID, Long> revisions;
    private final Set<UUID> refreshing = ConcurrentHashMap.newKeySet();
    private final BukkitTask reconciliationTask;
    private volatile boolean closed;


    public CacheManager() {
        superiorGenerator = SuperiorGenerator.get();
        database = superiorGenerator.getDatabase();
        generatorManager = superiorGenerator.getGeneratorManager();
        unlockedGenerators = new HashMap<>();
        activeGenerators = new HashMap<>();
        revisions = new HashMap<>();
        load();
        reconciliationTask = Bukkit.getScheduler().runTaskTimer(
                superiorGenerator, this::refreshOnlineIslands, 600L, 600L);
    }

    public void load() {
        unlockedGenerators.clear();
        activeGenerators.clear();

        for (Map.Entry<UUID, List<String>> entry : database.loadUnlockedGenerators().entrySet()) {
            List<AGenerator> generators = new ArrayList<>();
            for (String generatorID : entry.getValue()) {
                AGenerator generator = generatorManager.getGeneratorByID(generatorID);
                if (generator != null) generators.add(generator);
            }
            unlockedGenerators.put(entry.getKey(), generators);
        }

        for (Map.Entry<UUID, String> entry : database.loadActiveGenerators().entrySet()) {
            AGenerator generator = generatorManager.getGeneratorByID(entry.getValue());
            if (generator != null) {
                activeGenerators.put(entry.getKey(), generator);
            }
        }

        Bukkit.getLogger().info("[SuperiorGenerator] Loaded " + activeGenerators.size() + " active generators and " + unlockedGenerators.size() + " unlocked islands in cache.");
    }

    public void forceSelectGenerator(UUID islandUUID, AGenerator generator) {
        if (islandUUID == null) return;
        if (generator == null) return;
        activeGenerators.put(islandUUID, generator);
        touch(islandUUID);
    }

    public void forceUnlockGenerator(UUID islandUUID, AGenerator generator) {
        if (islandUUID == null) return;
        if (generator == null) return;

        List<AGenerator> unlocked = unlockedGenerators.computeIfAbsent(islandUUID, ignored -> new ArrayList<>());

        if (!unlocked.contains(generator)) {
            unlocked.add(generator);
        }
        activeGenerators.put(islandUUID, generator);
        touch(islandUUID);
    }

    public AGenerator getCurrentIslandGenerator(Player p) {
        UUID islandUuid = superiorGenerator.islands()
                .islandByMember(p)
                .map(IslandService.IslandInfo::uuid)
                .orElse(null);
        return getCurrentIslandGenerator(islandUuid);
    }

    public AGenerator getCurrentIslandGenerator(UUID islandUUID) {
        if (islandUUID == null) return generatorManager.getDefaultGenerator();
        return activeGenerators.getOrDefault(islandUUID, generatorManager.getDefaultGenerator());
    }

    public boolean selectIslandGenerator(UUID islandUUID, AGenerator generator) {
        if (islandUUID == null || generator == null) return false;
        AGenerator gen = activeGenerators.get(islandUUID);
        if (gen == generator) return false;
        if (!database.saveActiveGenerator(islandUUID, generator.getID())) return false;

        activeGenerators.put(islandUUID, generator);
        touch(islandUUID);
        superiorGenerator.getMessageBus().publish(new SelectAction(islandUUID, generator.getID()));
        return true;
    }

    public boolean unlockGenerator(UUID islandUUID, AGenerator generator) {
        if (islandUUID == null || generator == null || generator.getID().equals("default")) return false;

        List<AGenerator> unlockedGenerator = unlockedGenerators.get(islandUUID);
        if (unlockedGenerator == null) unlockedGenerator = new ArrayList<>();

        if (unlockedGenerator.contains(generator)) return false;
        if (!database.addOrEditUnlockedGenerator(islandUUID, generator.getID())) return false;

        unlockedGenerator.add(generator);
        unlockedGenerators.put(islandUUID, unlockedGenerator);
        activeGenerators.put(islandUUID, generator);
        touch(islandUUID);

        superiorGenerator.getMessageBus().publish(new UnlockAction(islandUUID, generator.getID()));
        return true;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        superiorGenerator.islands().islandByMember(p)
                .map(IslandService.IslandInfo::uuid)
                .ifPresent(this::refreshIsland);
    }

    private void refreshIsland(UUID islandUuid) {
        if (closed || !refreshing.add(islandUuid)) return;
        long revision = revisions.getOrDefault(islandUuid, 0L);
        Bukkit.getScheduler().runTaskAsynchronously(superiorGenerator, () -> {
            Optional<Database.IslandGenerators> loaded = database.loadIsland(islandUuid);
            if (closed || !superiorGenerator.isEnabled()) {
                refreshing.remove(islandUuid);
                return;
            }
            Bukkit.getScheduler().runTask(superiorGenerator, () -> {
                try {
                    loaded.ifPresent(snapshot -> applySnapshot(islandUuid, revision, snapshot));
                } finally {
                    refreshing.remove(islandUuid);
                }
            });
        });
    }

    private void applySnapshot(UUID islandUuid, long revision, Database.IslandGenerators snapshot) {
        if (revisions.getOrDefault(islandUuid, 0L) != revision) return;

        List<AGenerator> unlocked = snapshot.unlockedGeneratorIds().stream()
                .map(generatorManager::getGeneratorByID)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        if (unlocked.isEmpty()) unlockedGenerators.remove(islandUuid);
        else unlockedGenerators.put(islandUuid, unlocked);

        AGenerator active = snapshot.activeGeneratorId() == null
                ? null
                : generatorManager.getGeneratorByID(snapshot.activeGeneratorId());
        if (active == null) activeGenerators.remove(islandUuid);
        else activeGenerators.put(islandUuid, active);
        touch(islandUuid);
    }

    private void touch(UUID islandUuid) {
        revisions.merge(islandUuid, 1L, Long::sum);
    }

    private void refreshOnlineIslands() {
        Set<UUID> islands = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            superiorGenerator.islands().islandByMember(player)
                    .map(IslandService.IslandInfo::uuid)
                    .ifPresent(islands::add);
        }
        islands.forEach(this::refreshIsland);
    }

    public void close() {
        closed = true;
        reconciliationTask.cancel();
        refreshing.clear();
    }

    public boolean isAlreadyUnlocked(UUID islandUUID, AGenerator generator) {
        if (generator == null) return false;
        if (generator.getID().equals("default")) return true;
        List<AGenerator> generators = unlockedGenerators.get(islandUUID);
        if (generators == null) return false;

        return generators.contains(generator);
    }

    public List<AGenerator> getAllUnlockedGeneratorsOfIsland(UUID islandUUID) {
        List<AGenerator> generators = new ArrayList<>(unlockedGenerators.getOrDefault(islandUUID, List.of()));
        AGenerator defaultGenerator = generatorManager.getDefaultGenerator();
        if (!generators.contains(defaultGenerator)) generators.add(defaultGenerator);
        return generators;
    }


    public int getUnlockedCategoriesNumber(UUID islandUUID, String category) {

        List<AGenerator> generators = getAllUnlockedGeneratorsOfIsland(islandUUID).stream().filter(generator -> generator.getCategory().equals(category)).collect(Collectors.toList());
        return generators.size();
    }
}

package fr.nivcoo.superiorgenerator.storage;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.storage.model.ActiveGeneratorModel;
import fr.nivcoo.superiorgenerator.storage.model.UnlockedGeneratorModel;
import fr.nivcoo.utilsz.core.database.DatabaseManager;
import fr.nivcoo.utilsz.core.database.ModelRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Database {

    private final ModelRepository<ActiveGeneratorModel> activeGenerators;
    private final ModelRepository<UnlockedGeneratorModel> unlockedGenerators;

    public Database(DatabaseManager db) {
        this.activeGenerators = db.model(ActiveGeneratorModel.MODEL);
        this.unlockedGenerators = db.model(UnlockedGeneratorModel.MODEL);
    }

    public void initDB() throws SQLException {
        activeGenerators.createTable();
        unlockedGenerators.createTable();
    }

    public boolean addOrEditUnlockedGenerator(UUID islandUUID, String generatorID) {
        try {
            if (!unlockedGenerators.exists("island_uuid = ? AND generator_id = ?", islandUUID, generatorID)) {
                unlockedGenerators.insert(new UnlockedGeneratorModel(islandUUID, generatorID));
            }
            return saveActiveGenerator(islandUUID, generatorID);
        } catch (SQLException e) {
            log("addOrEditUnlockedGenerator", e);
            return false;
        }
    }

    public boolean saveActiveGenerator(UUID islandUUID, String generatorID) {
        try {
            if (activeGenerators.exists("island_uuid = ?", islandUUID)) {
                activeGenerators.update(Map.of("generator_id", generatorID), "island_uuid = ?", islandUUID);
            } else {
                activeGenerators.insert(new ActiveGeneratorModel(islandUUID, generatorID));
            }
            return true;
        } catch (SQLException e) {
            log("saveActiveGenerator", e);
            return false;
        }
    }

    public Map<UUID, String> loadActiveGenerators() {
        try {
            Map<UUID, String> active = new HashMap<>();
            for (ActiveGeneratorModel model : activeGenerators.all()) {
                active.put(model.islandUuid(), model.generatorId());
            }
            return active;
        } catch (SQLException e) {
            log("loadActiveGenerators", e);
            return Map.of();
        }
    }

    public Map<UUID, List<String>> loadUnlockedGenerators() {
        try {
            Map<UUID, List<String>> unlocked = new HashMap<>();
            for (UnlockedGeneratorModel model : unlockedGenerators.all()) {
                unlocked.computeIfAbsent(model.islandUuid(), ignored -> new ArrayList<>()).add(model.generatorId());
            }
            return unlocked;
        } catch (SQLException e) {
            log("loadUnlockedGenerators", e);
            return Map.of();
        }
    }

    private void log(String action, SQLException e) {
        SuperiorGenerator.get().getLogger().log(Level.SEVERE, "[SuperiorGenerator/DB] " + action, e);
    }
}

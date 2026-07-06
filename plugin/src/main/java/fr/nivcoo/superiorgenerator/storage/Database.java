package fr.nivcoo.superiorgenerator.storage;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.storage.model.ActiveGeneratorModel;
import fr.nivcoo.superiorgenerator.storage.model.UnlockedGeneratorModel;
import fr.nivcoo.utilsz.core.database.DatabaseManager;
import fr.nivcoo.utilsz.core.database.ModelRepository;

import java.sql.SQLException;
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

    public void addOrEditUnlockedGenerator(UUID islandUUID, String generatorID) {
        if (!hasAlreadyUnlock(islandUUID, generatorID)) {
            insertNewUnlockedGen(islandUUID, generatorID);
        }
        if (getCurrentIslandGeneratorID(islandUUID) == null) {
            insertNewActiveGen(islandUUID, generatorID);
        } else {
            updateActiveGen(islandUUID, generatorID);
        }
    }

    public void updateActiveGen(UUID islandUUID, String generatorID) {
        try {
            activeGenerators.update(Map.of("generator_id", generatorID), "island_uuid = ?", islandUUID);
        } catch (SQLException e) {
            log("updateActiveGen", e);
        }
    }

    public void insertNewActiveGen(UUID islandUUID, String generatorID) {
        try {
            activeGenerators.insert(new ActiveGeneratorModel(islandUUID, generatorID));
        } catch (SQLException e) {
            log("insertNewActiveGen", e);
        }
    }

    public void insertNewUnlockedGen(UUID islandUUID, String generatorID) {
        try {
            unlockedGenerators.insert(new UnlockedGeneratorModel(islandUUID, generatorID));
        } catch (SQLException e) {
            log("insertNewUnlockedGen", e);
        }
    }

    public boolean hasAlreadyUnlock(UUID islandUUID, String generatorID) {
        try {
            return unlockedGenerators.exists("island_uuid = ? AND generator_id = ?", islandUUID, generatorID);
        } catch (SQLException e) {
            log("hasAlreadyUnlock", e);
            return false;
        }
    }

    public String getCurrentIslandGeneratorID(UUID islandUUID) {
        try {
            ActiveGeneratorModel model = activeGenerators.find()
                    .where("island_uuid", islandUUID)
                    .limit(1)
                    .all()
                    .stream()
                    .findFirst()
                    .orElse(null);
            return model == null ? null : model.generatorId();
        } catch (SQLException e) {
            log("getCurrentIslandGeneratorID", e);
            return null;
        }
    }

    public List<String> getAllUnlockedIslandGeneratorID(UUID islandUUID) {
        try {
            return unlockedGenerators.find()
                    .where("island_uuid", islandUUID)
                    .all()
                    .stream()
                    .map(UnlockedGeneratorModel::generatorId)
                    .toList();
        } catch (SQLException e) {
            log("getAllUnlockedIslandGeneratorID", e);
            return List.of();
        }
    }

    private void log(String action, SQLException e) {
        SuperiorGenerator.get().getLogger().log(Level.SEVERE, "[SuperiorGenerator/DB] " + action, e);
    }
}

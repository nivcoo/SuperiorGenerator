package fr.nivcoo.superiorgenerator.utils;

import fr.nivcoo.utilsz.database.ColumnDefinition;
import fr.nivcoo.utilsz.database.DatabaseManager;
import fr.nivcoo.utilsz.database.DatabaseType;
import fr.nivcoo.utilsz.database.TableConstraintDefinition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {

    private final DatabaseManager db;

    public Database(DatabaseManager db) {
        this.db = db;
    }

    public void initDB() throws SQLException {
        db.createTable("active_generator", List.of(
                new ColumnDefinition("island_uuid", uuidType(), "PRIMARY KEY"),
                new ColumnDefinition("generator_id", textVarType(), "DEFAULT '0'")
        ));
        db.createTable("unlocked_generator", List.of(
                new ColumnDefinition("island_uuid", uuidType()),
                new ColumnDefinition("generator_id", textVarType()),
                new TableConstraintDefinition("PRIMARY KEY (island_uuid, generator_id)")
        ));
    }

    private String uuidType() {
        DatabaseType t = db.getType();
        return (t == DatabaseType.MYSQL || t == DatabaseType.MARIADB) ? "VARCHAR(36)" : "TEXT";
    }

    private String textVarType() {
        DatabaseType t = db.getType();
        return (t == DatabaseType.MYSQL || t == DatabaseType.MARIADB) ? "VARCHAR(64)" : "TEXT";
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
        String sql = "UPDATE active_generator SET generator_id=? WHERE island_uuid=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, generatorID);
            ps.setString(2, islandUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertNewActiveGen(UUID islandUUID, String generatorID) {
        String sql = "INSERT INTO active_generator(island_uuid, generator_id) VALUES(?,?)";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, islandUUID.toString());
            ps.setString(2, generatorID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertNewUnlockedGen(UUID islandUUID, String generatorID) {
        String sql = "INSERT INTO unlocked_generator(island_uuid, generator_id) VALUES(?,?)";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, islandUUID.toString());
            ps.setString(2, generatorID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasAlreadyUnlock(UUID islandUUID, String generatorID) {
        String sql = "SELECT 1 FROM active_generator WHERE island_uuid=? AND generator_id=? LIMIT 1";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, islandUUID.toString());
            ps.setString(2, generatorID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getCurrentIslandGeneratorID(UUID islandUUID) {
        String sql = "SELECT generator_id FROM active_generator WHERE island_uuid=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, islandUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("generator_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAllUnlockedIslandGeneratorID(UUID islandUUID) {
        List<String> out = new ArrayList<>();
        String sql = "SELECT generator_id FROM unlocked_generator WHERE island_uuid=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, islandUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("generator_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}

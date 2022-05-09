package fr.nivcoo.superiorgenerator.utils;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    private String DBPath;
    private Connection connection = null;
    private Statement statement = null;

    public Database(String dBPath) {
        DBPath = dBPath;
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            if (connection == null || connection.isClosed())
                connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            if (statement == null || statement.isClosed())
                statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (!connection.isClosed())
                connection.close();
            if (!statement.isClosed())
                statement.close();
        } catch (SQLException ignored) {
        }
    }

    public void initDB() {
        connect();
        query("CREATE TABLE IF NOT EXISTS active_generator (" + "island_uuid TEXT PRIMARY KEY, " + "generator_id TEXT DEFAULT 0 " + ")");
        query("CREATE TABLE IF NOT EXISTS unlocked_generator (" + "island_uuid TEXT, " + "generator_id TEXT " + ")");
        close();

    }

    public void query(String request) {
        try {
            statement.executeQuery(request);
        } catch (SQLException ignored) {
        }
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
        connect();
        try {
            PreparedStatement updateValues = connection.prepareStatement("UPDATE active_generator SET generator_id=? WHERE island_uuid=?");
            updateValues.setString(1, generatorID);
            updateValues.setString(2, islandUUID.toString());
            updateValues.executeUpdate();
            updateValues.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

    }

    public void insertNewActiveGen(UUID islandUUID, String generatorID) {
        connect();
        try {
            PreparedStatement insertValues = connection.prepareStatement("INSERT INTO active_generator VALUES(?,?)");
            insertValues.setString(1, islandUUID.toString());
            insertValues.setString(2, generatorID);
            insertValues.executeUpdate();
            insertValues.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

    }

    public void insertNewUnlockedGen(UUID islandUUID, String generatorID) {
        connect();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO unlocked_generator VALUES(?,?)");
            preparedStatement.setString(1, islandUUID.toString());
            preparedStatement.setString(2, generatorID);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

    }

    public boolean hasAlreadyUnlock(UUID islandUUID, String generatorID) {
        connect();
        boolean unlocked = false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM active_generator WHERE island_uuid=? AND generator_id=?");
            preparedStatement.setString(1, islandUUID.toString());
            preparedStatement.setString(2, generatorID);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                unlocked = true;
            }
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

        return unlocked;
    }


    public String getCurrentIslandGeneratorID(UUID islandUUID) {
        connect();
        String generatorID = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM active_generator WHERE island_uuid=?");
            preparedStatement.setString(1, islandUUID.toString());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                generatorID = rs.getString("generator_id");
            }
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

        return generatorID;
    }

    public List<String> getAllUnlockedIslandGeneratorID(UUID islandUUID) {
        connect();
        List<String> generatorsID = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM unlocked_generator WHERE island_uuid=?");
            preparedStatement.setString(1, islandUUID.toString());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                generatorsID.add(rs.getString("generator_id"));
            }
            preparedStatement.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();

        return generatorsID;
    }

}
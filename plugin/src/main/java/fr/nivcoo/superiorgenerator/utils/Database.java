package fr.nivcoo.superiorgenerator.utils;

import fr.nivcoo.utilsz.database.DatabaseType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private final DatabaseType dbType;
    private final String sqlitePath;
    private final String host, database, username, password;
    private final int port;
    private Connection connection = null;
    private Statement statement = null;

    public Database(DatabaseType dbType, String sqlitePath, String host, int port, String database, String username, String password) {
        this.dbType = dbType;
        this.sqlitePath = sqlitePath;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            if (dbType == DatabaseType.SQLITE) {
                Class.forName("org.sqlite.JDBC");
                if (connection == null || connection.isClosed())
                    connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
            } else if (dbType == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                if (connection == null || connection.isClosed()) {
                    String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
                    connection = DriverManager.getConnection(url, username, password);
                }
            }

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
        for (String query : getCreateTableStatements()) {
            this.update(query);
        }
        close();

    }

    public List<String> getCreateTableStatements() {
        List<String> queries = new ArrayList<>();

        if (dbType == DatabaseType.SQLITE) {
            queries.add("CREATE TABLE IF NOT EXISTS active_generator (island_uuid TEXT PRIMARY KEY, generator_id TEXT DEFAULT 0)");
            queries.add("CREATE TABLE IF NOT EXISTS unlocked_generator (island_uuid TEXT, generator_id TEXT)");
        } else {
            queries.add("CREATE TABLE IF NOT EXISTS active_generator (island_uuid VARCHAR(36) PRIMARY KEY, generator_id VARCHAR(64) DEFAULT '0')");
            queries.add("CREATE TABLE IF NOT EXISTS unlocked_generator (island_uuid VARCHAR(36), generator_id VARCHAR(64), PRIMARY KEY (island_uuid, generator_id))");
        }

        return queries;
    }


    public void update(String request) {
        try {
            statement.executeUpdate(request);
        } catch (SQLException e) {
            e.printStackTrace();
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
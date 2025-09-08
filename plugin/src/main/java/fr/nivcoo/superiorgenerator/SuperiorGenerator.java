package fr.nivcoo.superiorgenerator;

import fr.nivcoo.superiorgenerator.actions.SelectAction;
import fr.nivcoo.superiorgenerator.actions.UnlockAction;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.commands.SelectCMD;
import fr.nivcoo.superiorgenerator.command.commands.UnlockCMD;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.listener.BlockListener;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.placeholder.PlaceHolderAPI;
import fr.nivcoo.superiorgenerator.utils.Database;
import fr.nivcoo.superiorgeneratorapi.ASuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.SuperiorGeneratorAPI;
import fr.nivcoo.utilsz.commands.CommandManager;
import fr.nivcoo.utilsz.config.Config;
import fr.nivcoo.utilsz.database.DatabaseManager;
import fr.nivcoo.utilsz.database.DatabaseType;
import fr.nivcoo.utilsz.redis.RedisChannelRegistry;
import fr.nivcoo.utilsz.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SuperiorGenerator extends JavaPlugin implements ASuperiorGenerator {

    private static SuperiorGenerator INSTANCE;
    private Config config;

    private DatabaseManager databaseManager;
    private Database database;

    private SuperiorSkyblock2 superiorSkyblock2;
    private GeneratorManager generatorManager;
    private CacheManager cacheManager;
    private RedisManager redisManager;
    private final Logger log = getLogger();

    RedisChannelRegistry tagChannel;

    @Override
    public void onEnable() {
        INSTANCE = this;
        loadAPI();
        config = new Config(loadFile("config.yml"));

        String type = config.getString("database.type", "sqlite").toLowerCase();
        DatabaseType dbType = switch (type) {
            case "mysql" -> DatabaseType.MYSQL;
            case "mariadb" -> DatabaseType.MARIADB;
            default -> DatabaseType.SQLITE;
        };

        String sqlitePath = new File(getDataFolder(), config.getString("database.sqlite.path", "database.db")).getPath();

        databaseManager = new DatabaseManager(
                dbType,
                config.getString("database.mysql.host"),
                config.getInt("database.mysql.port"),
                config.getString("database.mysql.database"),
                config.getString("database.mysql.username"),
                config.getString("database.mysql.password"),
                sqlitePath
        );

        database = new Database(databaseManager);

        try {
            database.initDB();
        } catch (SQLException e) {
            getLogger().warning("SuperiorGenerator: table init error: " + e.getMessage());
        }

        superiorSkyblock2 = new SuperiorSkyblock2();
        Bukkit.getPluginManager().registerEvents(superiorSkyblock2, this);

        generatorManager = new GeneratorManager();
        cacheManager = new CacheManager();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderAPI().register();
        }

        Bukkit.getPluginManager().registerEvents(cacheManager, this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        CommandManager commandManager = new CommandManager(this, config, "generator", "superiorgenerator.commands");
        commandManager.addCommand(new UnlockCMD());
        commandManager.addCommand(new SelectCMD());

        if (config.getBoolean("redis.enabled")) {
            redisManager = new RedisManager(
                    this,
                    config.getString("redis.host"),
                    config.getInt("redis.port"),
                    config.getString("redis.username"),
                    config.getString("redis.password")
            );

            tagChannel = redisManager.createRegistry("superiorgenerator-update");
            tagChannel.register(SelectAction.class);
            tagChannel.register(UnlockAction.class);

            getLogger().info("Redis activé et connecté à " + config.getString("redis.host") + ":" + config.getInt("redis.port"));
        } else {
            getLogger().info("Redis désactivé dans la configuration.");
        }
    }

    @Override
    public void onDisable() {
        if (redisManager != null) redisManager.close();
        if (databaseManager != null) databaseManager.closeConnection();
    }

    private void loadAPI() {
        try {
            Field instance = SuperiorGeneratorAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Config getConfiguration() {
        return config;
    }

    private File loadFile(String path) {
        File configFile = new File(getDataFolder(), path);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource(path, false);
        }
        return configFile;
    }

    public Database getDatabase() {
        return database;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SuperiorSkyblock2 getSuperiorSkyblock2() {
        return superiorSkyblock2;
    }

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public static SuperiorGenerator get() {
        return INSTANCE;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public boolean isRedisEnabled() {
        return redisManager != null;
    }

    public RedisChannelRegistry getTagChannel() {
        return tagChannel;
    }
}

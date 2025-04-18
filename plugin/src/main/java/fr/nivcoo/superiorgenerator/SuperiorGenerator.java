package fr.nivcoo.superiorgenerator;

import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.commands.SelectCMD;
import fr.nivcoo.superiorgenerator.command.commands.UnlockCMD;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.listener.BlockListener;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.placeholder.PlaceHolderAPI;
import fr.nivcoo.superiorgenerator.redis.GeneratorRedisMessage;
import fr.nivcoo.superiorgenerator.utils.Database;
import fr.nivcoo.superiorgenerator.utils.DatabaseType;
import fr.nivcoo.superiorgeneratorapi.ASuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.SuperiorGeneratorAPI;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.commands.CommandManager;
import fr.nivcoo.utilsz.config.Config;
import fr.nivcoo.utilsz.redis.RedisChannelRegistry;
import fr.nivcoo.utilsz.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

public class SuperiorGenerator extends JavaPlugin implements ASuperiorGenerator {

    private static SuperiorGenerator INSTANCE;
    private Config config;
    private Database database;
    private SuperiorSkyblock2 superiorSkyblock2;
    private GeneratorManager generatorManager;
    private CacheManager cacheManager;
    private CommandManager commandManager;
    private RedisManager redisManager;
    private final Logger log = getLogger();

    RedisChannelRegistry tagChannel;

    @Override
    public void onEnable() {
        INSTANCE = this;
        loadAPI();
        config = new Config(loadFile("config.yml"));


        String type = config.getString("database.type").toLowerCase();
        DatabaseType dbType = type.equals("mysql") ? DatabaseType.MYSQL : DatabaseType.SQLITE;

        if (dbType == DatabaseType.SQLITE) {
            File db = new File(getDataFolder(), config.getString("database.sqlite.path"));
            if (!db.exists()) {
                try {
                    db.createNewFile();
                } catch (IOException ignored) {
                }
            }
            database = new Database(dbType, db.getPath(), null, 0, null, null, null);
        } else {
            database = new Database(
                    dbType,
                    null,
                    config.getString("database.mysql.host"),
                    config.getInt("database.mysql.port"),
                    config.getString("database.mysql.database"),
                    config.getString("database.mysql.username"),
                    config.getString("database.mysql.password")
            );
        }

        database.initDB();


        superiorSkyblock2 = new SuperiorSkyblock2();
        Bukkit.getPluginManager().registerEvents(superiorSkyblock2, this);

        generatorManager = new GeneratorManager();

        cacheManager = new CacheManager();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderAPI().register();
        }

        Bukkit.getPluginManager().registerEvents(cacheManager, this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);


        commandManager = new CommandManager(this, config, "generator", "superiorgenerator.commands");
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
            redisManager.start();

            tagChannel = new RedisChannelRegistry(redisManager, "superiorgenerator-update");

            tagChannel.register("select", GeneratorRedisMessage::new, this::handleSelect);
            tagChannel.register("unlock", GeneratorRedisMessage::new, this::handleUnlock);


            getLogger().info("Redis activé et connecté à " + config.getString("redis.host") + ":" + config.getInt("redis.port"));
        } else {
            getLogger().info("Redis désactivé dans la configuration.");
        }
    }

    private void handleSelect(GeneratorRedisMessage msg) {
        AGenerator generator = generatorManager.getGeneratorByID(msg.generatorID());
        if (generator != null) {
            cacheManager.forceSelectGenerator(msg.islandUUID(), generator);
            log.info("Generator " + generator.getID() + " sélectionné pour l'île " + msg.islandUUID());
        }
    }

    private void handleUnlock(GeneratorRedisMessage msg) {
        AGenerator generator = generatorManager.getGeneratorByID(msg.generatorID());
        if (generator != null) {
            cacheManager.forceUnlockGenerator(msg.islandUUID(), generator);
            log.info("Generator " + generator.getID() + " débloqué pour l'île " + msg.islandUUID());
        }
    }

    @Override
    public void onDisable() {

        if (redisManager != null) redisManager.close();
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

package fr.nivcoo.superiorgenerator;

import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.commands.SelectCMD;
import fr.nivcoo.superiorgenerator.command.commands.UnlockCMD;
import fr.nivcoo.superiorgenerator.listener.BlockListener;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.placeholder.PlaceHolderAPI;
import fr.nivcoo.superiorgenerator.utils.Database;
import fr.nivcoo.utilsz.commands.CommandManager;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class SuperiorGenerator extends JavaPlugin {

    private static SuperiorGenerator INSTANCE;
    private Config config;
    private Database database;
    private GeneratorManager generatorManager;
    private CacheManager cacheManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        INSTANCE = this;
        config = new Config(loadFile("config.yml"));
        File db = new File(getDataFolder(), "database.db");
        if (!db.exists()) {
            try {
                db.createNewFile();
            } catch (IOException ignored) {
            }
        }
        database = new Database(db.getPath());
        database.initDB();

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

    }

    @Override
    public void onDisable() {
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

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public static SuperiorGenerator get() {
        return INSTANCE;
    }

}

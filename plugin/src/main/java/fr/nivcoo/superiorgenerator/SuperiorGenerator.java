package fr.nivcoo.superiorgenerator;

import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.SelectCommand;
import fr.nivcoo.superiorgenerator.command.UnlockCommand;
import fr.nivcoo.superiorgenerator.config.GeneratorsConfig;
import fr.nivcoo.superiorgenerator.config.MainConfig;
import fr.nivcoo.superiorgenerator.config.MessagesConfig;
import fr.nivcoo.superiorgenerator.hook.core.HookContext;
import fr.nivcoo.superiorgenerator.hook.integration.InsightsHook;
import fr.nivcoo.superiorgenerator.hook.platform.SuperiorHook;
import fr.nivcoo.superiorgenerator.listener.BlockListener;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.messaging.action.SelectAction;
import fr.nivcoo.superiorgenerator.messaging.action.UnlockAction;
import fr.nivcoo.superiorgenerator.placeholder.PlaceHolderAPI;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.superiorgenerator.storage.Database;
import fr.nivcoo.superiorgeneratorapi.ASuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.SuperiorGeneratorAPI;
import fr.nivcoo.utilsz.core.commands.CommandManager;
import fr.nivcoo.utilsz.core.commands.CommandsConfigProvider;
import fr.nivcoo.utilsz.core.commands.SimpleCommandsConfig;
import fr.nivcoo.utilsz.core.config.ConfigManager;
import fr.nivcoo.utilsz.core.database.DatabaseManager;
import fr.nivcoo.utilsz.core.messaging.MessageBus;
import fr.nivcoo.utilsz.platform.bukkit.commands.BukkitCommandRegistrar;
import fr.nivcoo.utilsz.platform.bukkit.hook.BukkitHook;
import fr.nivcoo.utilsz.platform.bukkit.hook.BukkitHookRegistry;
import fr.nivcoo.utilsz.platform.bukkit.tracking.BlockChangeService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class SuperiorGenerator extends JavaPlugin implements ASuperiorGenerator {

    private static SuperiorGenerator INSTANCE;

    private MainConfig config;
    private MessagesConfig messages;
    private GeneratorsConfig generators;
    private DatabaseManager databaseManager;
    private Database database;
    private HookContext hookContext;
    private IslandService islandService;
    private BlockChangeService blockChangeService;
    private GeneratorManager generatorManager;
    private CacheManager cacheManager;
    private MessageBus messageBus;

    @Override
    public void onEnable() {
        INSTANCE = this;
        SuperiorGeneratorAPI.set(this);

        loadConfigs();
        databaseManager = config.database.createManager(getDataFolder());
        database = new Database(databaseManager);

        try {
            database.initDB();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize SuperiorGenerator database tables", e);
        }

        islandService = new IslandService();
        blockChangeService = new BlockChangeService(this);
        setupHooks();

        generatorManager = new GeneratorManager();
        cacheManager = new CacheManager();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderAPI().register();
        }

        Bukkit.getPluginManager().registerEvents(cacheManager, this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        CommandsConfigProvider provider = new SimpleCommandsConfig(
                messages.commands.noPermission,
                messages.commands.incorrectUsage,
                messages.commands.help
        );
        CommandManager commandManager = new CommandManager(
                new BukkitCommandRegistrar(this),
                provider,
                "generator",
                "superiorgenerator.commands"
        );
        commandManager.addCommand(new UnlockCommand());
        commandManager.addCommand(new SelectCommand());

        messageBus = config.messaging.createBus(runnable -> Bukkit.getScheduler().runTask(this, runnable), getSLF4JLogger());
        messageBus.register(SelectAction.class);
        messageBus.register(UnlockAction.class);
        messageBus.start();
    }

    private void setupHooks() {
        hookContext = new HookContext(this);
        new BukkitHookRegistry<>(List.<Function<HookContext, BukkitHook<HookContext>>>of(
                SuperiorHook::new,
                InsightsHook::new
        )).loadAll(hookContext);
    }

    private void loadConfigs() {
        ConfigManager cm = new ConfigManager(getDataFolder());
        config = cm.load("config.yml", MainConfig.class);
        messages = cm.load("messages.yml", MessagesConfig.class);
        generators = cm.load("generators.yml", GeneratorsConfig.class);
    }

    @Override
    public void onDisable() {
        SuperiorGeneratorAPI.set(null);
        if (hookContext != null) hookContext.cancelTasks();
        if (messageBus != null) messageBus.close();
        if (databaseManager != null) databaseManager.closeConnection();
    }

    public MainConfig getConfiguration() {
        return config;
    }

    public MainConfig cfg() {
        return config;
    }

    public MessagesConfig messages() {
        return messages;
    }

    public GeneratorsConfig generators() {
        return generators;
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

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public IslandService islands() {
        return islandService;
    }

    public BlockChangeService blockChanges() {
        return blockChangeService;
    }

    public static SuperiorGenerator get() {
        return INSTANCE;
    }
}

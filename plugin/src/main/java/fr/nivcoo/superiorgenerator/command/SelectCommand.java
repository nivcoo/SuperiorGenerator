package fr.nivcoo.superiorgenerator.command;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.config.MessagesConfig;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.core.config.ConfigManager;
import fr.nivcoo.utilsz.platform.bukkit.commands.BukkitCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectCommand implements BukkitCommand {

    private static final String ADMIN_SELECT_PERMISSION = "superiorgenerator.admin.command.select.other";

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("select");
    }

    @Override
    public String getPermission() {
        return "superiorgenerator.command.select";
    }

    @Override
    public String getUsage() {
        return "select <generator>";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        SuperiorGenerator plugin = SuperiorGenerator.get();
        MessagesConfig.Select messages = plugin.messages().commands.select;
        boolean selectForOtherIsland = sender.hasPermission(ADMIN_SELECT_PERMISSION) && args.length == 3;
        Player player = (Player) sender;

        if (selectForOtherIsland) {
            String playerString = args[2];
            player = Bukkit.getPlayer(playerString);
            if (player == null) {
                sender.sendMessage(fmt(messages.other.notFoundPlayer, "player", playerString));
                return;
            }
        }

        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        String generatorID = args[1];
        AGenerator generator = generatorManager.getGeneratorByID(generatorID);
        IslandService.IslandInfo island = plugin.islands().islandByMember(player).orElse(null);

        if (island == null) {
            sender.sendMessage(selectForOtherIsland ? messages.other.noIsland : messages.noIsland);
            return;
        }

        if (!selectForOtherIsland && !plugin.islands().hasPermission(player, island.uuid(), IslandService.MANAGE_GENERATOR_PERMISSION)) {
            sender.sendMessage(messages.noPermission);
            return;
        }

        UUID islandUUID = island.uuid();
        Component returnMessage;
        if (cacheManager.isAlreadyUnlocked(islandUUID, generator)) {
            if (cacheManager.selectIslandGenerator(islandUUID, generator)) {
                returnMessage = messages.success;
            } else {
                returnMessage = messages.alreadySelected;
            }
        } else {
            returnMessage = messages.notUnlocked;
        }

        sender.sendMessage(returnMessage);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission(ADMIN_SELECT_PERMISSION)) {
            if (args.length == 2) return getAllGeneratorsName();
            if (args.length == 3) return getOnlinePlayersNames();
        } else if (args.length == 2 && sender instanceof Player player) {
            UUID islandUUID = SuperiorGenerator.get().islands()
                    .islandByMember(player)
                    .map(island -> island.uuid())
                    .orElse(null);
            return getUnlockedGeneratorsName(islandUUID);
        }
        return new ArrayList<>();
    }

    private List<String> getOnlinePlayersNames() {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
        return players;
    }

    private List<String> getUnlockedGeneratorsName(UUID islandUUID) {
        List<String> generatorsName = new ArrayList<>();
        for (AGenerator generator : SuperiorGenerator.get().getCacheManager().getAllUnlockedGeneratorsOfIsland(islandUUID)) {
            generatorsName.add(generator.getID());
        }
        return generatorsName;
    }

    private List<String> getAllGeneratorsName() {
        List<String> generatorsName = new ArrayList<>();
        for (AGenerator generator : SuperiorGenerator.get().getGeneratorManager().getAllGenerators()) {
            generatorsName.add(generator.getID());
        }
        return generatorsName;
    }

    private Component fmt(Component component, Object... entries) {
        return ConfigManager.fmt(component, map(entries));
    }

    private Map<String, Object> map(Object... entries) {
        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            values.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return values;
    }
}

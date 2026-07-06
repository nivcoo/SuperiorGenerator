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

public class UnlockCommand implements BukkitCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("unlock");
    }

    @Override
    public String getPermission() {
        return "superiorgenerator.command.unlock";
    }

    @Override
    public String getUsage() {
        return "unlock <player> <generator>";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        SuperiorGenerator plugin = SuperiorGenerator.get();
        MessagesConfig.Unlock messages = plugin.messages().commands.unlock;
        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        Player player = Bukkit.getPlayer(args[1]);

        if (player == null) {
            sender.sendMessage(fmt(messages.notFoundPlayer, "player", args[1]));
            return;
        }
        IslandService.IslandInfo island = plugin.islands().islandByMember(player).orElse(null);
        if (island == null) {
            sender.sendMessage(fmt(messages.noIsland, "player", player.getName()));
            return;
        }
        String generatorID = args[2];
        AGenerator generator = generatorManager.getGeneratorByID(generatorID);
        if (generator == null) {
            sender.sendMessage(fmt(messages.notFound, "generator", generatorID));
            return;
        }

        UUID islandUUID = island.uuid();
        Component returnMessage;
        if (cacheManager.unlockGenerator(islandUUID, generator)) {
            returnMessage = fmt(messages.success, "generator", generatorID, "player", player.getName());
        } else {
            returnMessage = fmt(messages.alreadyUnlock, "generator", generatorID, "player", player.getName());
        }

        sender.sendMessage(returnMessage);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if (args.length == 2) return getOnlinePlayersNames();
        if (args.length == 3) return getGeneratorsName();
        return new ArrayList<>();
    }

    private List<String> getOnlinePlayersNames() {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
        return players;
    }

    private List<String> getGeneratorsName() {
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

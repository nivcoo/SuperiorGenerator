package fr.nivcoo.superiorgenerator.command.commands;


import com.bgsoftware.superiorskyblock.api.island.Island;
import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.CCommand;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.Generator;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UnlockCMD implements CCommand {

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

    public void execute(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        Config config = plugin.getConfiguration();
        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        Player player = Bukkit.getPlayer(args[1]);

        if (player == null) {
            sender.sendMessage(config.getString("messages.commands.unlock.not_found_player", args[1]));
            return;
        }
        Island island = SuperiorSkyblock2.getIslandByMember(player);
        if (island == null) {
            sender.sendMessage(config.getString("messages.commands.unlock.no_island", player.getName()));
            return;
        }
        String generatorID = args[2];
        Generator generator = generatorManager.getGeneratorByID(generatorID);
        if (generator == null) {
            sender.sendMessage(config.getString("messages.commands.unlock.not_found", generatorID));
            return;
        }

        UUID islandUUID = island.getUniqueId();
        String returnMessage;
        if (cacheManager.unlockGenerator(islandUUID, generatorManager.getGeneratorByID(generatorID)))
            returnMessage = config.getString("messages.commands.unlock.success", generatorID, player.getName());
        else {
            returnMessage = config.getString("messages.commands.unlock.already_unlock", generatorID, player.getName());
        }

        if (!returnMessage.equals(""))
            sender.sendMessage(returnMessage);
    }

    @Override
    public List<String> tabComplete(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        if (args.length == 2)
            return getOnlinePlayersNames();
        else if (args.length == 3)
            return getGeneratorsName();
        return new ArrayList<>();
    }

}

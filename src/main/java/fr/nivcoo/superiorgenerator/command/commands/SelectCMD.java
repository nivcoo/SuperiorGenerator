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

public class SelectCMD implements CCommand {

    private String adminSelectPermission = "superiorgenerator.admin.command.select.other";

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

    public void execute(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        boolean selectForOtherIsland = sender.hasPermission(adminSelectPermission) && args.length == 3;
        String messagePath = "messages.commands.select.";
        Player player = (Player) sender;
        Config config = plugin.getConfiguration();

        if (selectForOtherIsland) {
            String playerString = args[2];
            player = Bukkit.getPlayer(playerString);
            if (player == null) {
                sender.sendMessage(config.getString(messagePath + "other.not_found_player", playerString));
                return;
            }
        }

        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        String generatorID = args[1];
        Generator generator = generatorManager.getGeneratorByID(generatorID);
        Island island = SuperiorSkyblock2.getIslandByMember(player);

        if (island == null) {
            if (selectForOtherIsland)
                sender.sendMessage(config.getString(messagePath + "other.no_island"));
            else
                sender.sendMessage(config.getString(messagePath + "no_island"));
            return;
        }

        if (!selectForOtherIsland && !island.hasPermission(player, plugin.getSuperiorSkyblock2().getManageGeneratorPermission())) {
            sender.sendMessage(config.getString(messagePath + "no_permission"));
            return;
        }


        String playerName = player.getName();
        UUID islandUUID = island.getUniqueId();
        String returnMessage;
        if (cacheManager.isAlreadyUnlocked(islandUUID, generator)) {
            if (cacheManager.selectIslandGenerator(islandUUID, generator))
                returnMessage = config.getString(messagePath + "success", generatorID, playerName);

            else
                returnMessage = config.getString(messagePath + "already_selected", generatorID, playerName);

        } else
            returnMessage = config.getString(messagePath + "not_unlocked", generatorID, playerName);

        if (!returnMessage.equals(""))
            sender.sendMessage(returnMessage);
    }

    @Override
    public List<String> tabComplete(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        if (sender.hasPermission(adminSelectPermission)) {
            if (args.length == 2) {
                return getAllGeneratorsName();
            } else if (args.length == 3) {
                return getOnlinePlayersNames();
            }
        } else if (args.length == 2) {
            Player player = (Player) sender;
            UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(player);
            return getUnlockedGeneratorsName(islandUUID);
        }
        return new ArrayList<>();
    }

}

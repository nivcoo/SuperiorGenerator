package fr.nivcoo.superiorgenerator.command.commands;


import com.bgsoftware.superiorskyblock.api.island.Island;
import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.command.CCommand;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.Generator;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SelectCMD implements CCommand {

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
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    public void execute(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Config config = plugin.getConfiguration();
        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        String generatorID = args[1];
        Generator generator = generatorManager.getGeneratorByID(generatorID);
        Island island = SuperiorSkyblock2.getIslandByMember(player);
        if (island == null) {
            sender.sendMessage(config.getString("messages.commands.select.no_island"));
            return;
        }

        if (!island.hasPermission(player, plugin.getSuperiorSkyblock2().getManageGeneratorPermission())) {
            sender.sendMessage(config.getString("messages.commands.select.no_permission"));
            return;
        }


        String playerName = player.getName();
        UUID islandUUID = island.getUniqueId();
        if (cacheManager.getIfUnlocked(islandUUID, generator)) {
            if (cacheManager.selectIslandGenerator(islandUUID, generator))
                sender.sendMessage(config.getString("messages.commands.select.success", generatorID, playerName));
            else
                sender.sendMessage(config.getString("messages.commands.select.already_selected", generatorID, playerName));
        } else
            sender.sendMessage(config.getString("messages.commands.select.not_unlocked", generatorID, playerName));

    }

    @Override
    public List<String> tabComplete(SuperiorGenerator plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            Player player = (Player) sender;
            UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(player);
            return getUnlockedGeneratorsName(islandUUID);
        }
        return new ArrayList<>();
    }

}

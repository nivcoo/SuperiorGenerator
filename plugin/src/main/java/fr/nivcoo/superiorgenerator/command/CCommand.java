package fr.nivcoo.superiorgenerator.command;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface CCommand extends Command {

    default void execute(JavaPlugin plugin, CommandSender sender, String[] args) {
        execute((SuperiorGenerator) plugin, sender, args);
    }

    default List<String> tabComplete(JavaPlugin plugin, CommandSender sender, String[] args) {
        return tabComplete((SuperiorGenerator) plugin, sender, args);
    }

    void execute(SuperiorGenerator plugin, CommandSender sender, String[] args);

    List<String> tabComplete(SuperiorGenerator plugin, CommandSender sender, String[] args);

    default List<String> getOnlinePlayersNames() {
        List<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p.getName());
        }
        return players;
    }

    default List<String> getGeneratorsName() {
        List<String> generatorsName = new ArrayList<>();
        for (AGenerator generator : SuperiorGenerator.get().getGeneratorManager().getAllGenerators()) {
            generatorsName.add(generator.getID());
        }
        return generatorsName;
    }

    default List<String> getUnlockedGeneratorsName(UUID islandUUID) {
        List<String> generatorsName = new ArrayList<>();
        for (AGenerator generator : SuperiorGenerator.get().getCacheManager().getAllUnlockedGeneratorsOfIsland(islandUUID)) {
            generatorsName.add(generator.getID());
        }
        return generatorsName;
    }

    default List<String> getAllGeneratorsName() {
        List<String> generatorsName = new ArrayList<>();
        for (AGenerator generator : SuperiorGenerator.get().getGeneratorManager().getAllGenerators()) {
            generatorsName.add(generator.getID());
        }
        return generatorsName;
    }
}

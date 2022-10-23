package fr.nivcoo.superiorgenerator.hook.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class SuperiorSkyblock2 implements Listener {


    private IslandPrivilege MANAGE_GENERATOR;

    @EventHandler
    public void init(PluginInitializeEvent e) {
        IslandPrivilege.register("MANAGE_GENERATOR");
        MANAGE_GENERATOR = IslandPrivilege.getByName("MANAGE_GENERATOR");
    }

    public IslandPrivilege getManageGeneratorPermission() {
        if (MANAGE_GENERATOR == null)
            MANAGE_GENERATOR = IslandPrivilege.getByName("MANAGE_GENERATOR");
        return MANAGE_GENERATOR;
    }

    public static UUID getIslandUUIDByMember(Player p) {

        Island island = getIslandByMember(p);

        if (island == null)
            return null;

        return island.getUniqueId();

    }

    public static Island getIslandByMember(Player p) {

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(p);
        if (superiorPlayer == null)
            return null;

        return superiorPlayer.getIsland();

    }

    public static UUID getIslandUUIDByLocation(Location loc) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        if (island == null)
            return null;
        return island.getUniqueId();

    }

    public static void addBlockInIsland(Block block) {

        Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        if (island != null)
            island.handleBlockPlace(block);
    }

}

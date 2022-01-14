package fr.nivcoo.superiorgenerator.hook.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SuperiorSkyblock2 {

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

package fr.nivcoo.superiorgenerator.hook.platform;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import fr.nivcoo.superiorgenerator.hook.core.HookContext;
import fr.nivcoo.utilsz.platform.bukkit.hook.ListenerBukkitHook;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public final class SuperiorHook extends ListenerBukkitHook<HookContext> {

    private static IslandPrivilege manageGenerator;

    public SuperiorHook(HookContext context) {
    }

    @Override
    public String id() {
        return "SuperiorSkyblock2";
    }

    @Override
    public String requiredPlugin() {
        return "SuperiorSkyblock2";
    }

    @EventHandler
    public void init(PluginInitializeEvent event) {
        IslandPrivilege.register("MANAGE_GENERATOR");
        manageGenerator = IslandPrivilege.getByName("MANAGE_GENERATOR");
    }

    public static IslandPrivilege getManageGeneratorPermission() {
        if (manageGenerator == null) {
            manageGenerator = IslandPrivilege.getByName("MANAGE_GENERATOR");
        }
        return manageGenerator;
    }

    public static UUID getIslandUUIDByMember(Player player) {
        Island island = getIslandByMember(player);
        return island == null ? null : island.getUniqueId();
    }

    public static Island getIslandByMember(Player player) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        return superiorPlayer == null ? null : superiorPlayer.getIsland();
    }

    public static UUID getIslandUUIDByLocation(Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        return island == null ? null : island.getUniqueId();
    }

    public static void addBlockInIsland(Block block) {
        Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        if (island != null) {
            island.handleBlockPlace(block);
        }
    }
}

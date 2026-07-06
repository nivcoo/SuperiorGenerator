package fr.nivcoo.superiorgenerator.hook.platform;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import fr.nivcoo.superiorgenerator.hook.core.HookContext;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.utilsz.platform.bukkit.hook.ListenerBukkitHook;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class SuperiorHook extends ListenerBukkitHook<HookContext> {

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

    @Override
    protected void onLoad(HookContext context) {
        registerPrivilege(IslandService.MANAGE_GENERATOR_PERMISSION);
        context.plugin().islands().resolver(new IslandService.IslandResolver() {
            @Override
            public Optional<IslandService.IslandInfo> islandAt(Location location) {
                Island island = SuperiorSkyblockAPI.getIslandAt(location);
                return island == null ? Optional.empty() : Optional.of(new IslandService.IslandInfo(island.getUniqueId(), island.isSpawn()));
            }

            @Override
            public Optional<IslandService.IslandInfo> islandByMember(Player player) {
                if (player == null) return Optional.empty();
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
                if (superiorPlayer == null) return Optional.empty();
                Island island = superiorPlayer.getIsland();
                return island == null ? Optional.empty() : Optional.of(new IslandService.IslandInfo(island.getUniqueId(), island.isSpawn()));
            }

            @Override
            public boolean hasPermission(Player player, UUID islandUuid, String permission) {
                if (player == null || islandUuid == null || permission == null || permission.isBlank()) return false;
                Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUuid);
                if (island == null) return false;
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
                if (superiorPlayer == null) return false;
                IslandPrivilege privilege = privilege(permission);
                return privilege != null && island.hasPermission(superiorPlayer, privilege);
            }

            @Override
            public void handleBlockPlace(Block block) {
                if (block == null) return;
                Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
                if (island != null) island.handleBlockPlace(block);
            }
        });
    }

    private void registerPrivilege(String permission) {
        if (permission == null || permission.isBlank()) return;
        if (privilege(permission) != null) return;
        try {
            IslandPrivilege.register(permission, IslandPrivilege.Type.ACTION);
        } catch (RuntimeException ignored) {
        }
    }

    private IslandPrivilege privilege(String permission) {
        try {
            return IslandPrivilege.getByName(permission);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}

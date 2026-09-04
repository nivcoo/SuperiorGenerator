package fr.nivcoo.superiorgenerator.hook.platform;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import fr.nivcoo.superiorgenerator.hook.core.HookContext;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.utilsz.platform.bukkit.hook.ListenerBukkitHook;
import fr.nivcoo.utilsz.platform.bukkit.tracking.BlockChangeProvider;
import fr.nivcoo.utilsz.platform.bukkit.tracking.BlockChangeService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
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
        WorldRecordService worldRecords = context.plugin().getServer().getServicesManager()
                .load(WorldRecordService.class);
        if (worldRecords == null) {
            context.plugin().getLogger().warning(
                    "SuperiorSkyblock2 world-record service is unavailable; generator block tracking was not registered.");
        } else {
            context.plugin().blockChanges().register(new SuperiorBlockChangeProvider(worldRecords));
        }
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
        });
    }

    private record SuperiorBlockChangeProvider(WorldRecordService worldRecords) implements BlockChangeProvider {

        private static final int SAVE = WorldRecordFlags.SAVE_BLOCK_COUNT;
        private static final int SAVE_AND_DIRTY = SAVE | WorldRecordFlags.DIRTY_CHUNKS;

        @Override
        public String id() {
            return "SuperiorSkyblock2";
        }

        @Override
        public void apply(BlockChangeService.Changes changes) {
            if (SuperiorSkyblockAPI.getIslandAt(changes.location()) == null) return;
            KeyMap<Integer> broken = keys(changes.broken());
            KeyMap<Integer> placed = keys(changes.placed());
            if (!broken.isEmpty()) {
                worldRecords.recordMultiBlocksBreak(
                        broken, changes.location(), placed.isEmpty() ? SAVE_AND_DIRTY : SAVE);
            }
            if (!placed.isEmpty()) {
                worldRecords.recordMultiBlocksPlace(placed, changes.location(), SAVE_AND_DIRTY);
            }
        }

        private static KeyMap<Integer> keys(Map<Material, Integer> counts) {
            KeyMap<Integer> keys = KeyMap.createKeyMap();
            counts.forEach((material, amount) -> keys.merge(
                    SuperiorSkyblockAPI.getKeys().getKey(material), amount, Integer::sum));
            return keys;
        }
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

package fr.nivcoo.superiorgenerator.listener;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.GeneratorBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.UUID;

public class BlockListener implements Listener {

    private static final BlockFace[] GENERATOR_FACES = {
            BlockFace.WEST,
            BlockFace.EAST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.UP
    };

    private final SuperiorGenerator superiorGenerator;
    private final CacheManager cacheManager;
    private final GeneratorManager generatorManager;

    public BlockListener() {
        superiorGenerator = SuperiorGenerator.get();
        cacheManager = superiorGenerator.getCacheManager();
        generatorManager = superiorGenerator.getGeneratorManager();
    }

    private void generateRandomBlock(BlockState newState, UUID islandUUID) {
        AGenerator generator = cacheManager.getCurrentIslandGenerator(islandUUID);
        GeneratorBlock selectedBlock = generatorManager.getRandomBlock(generator);

        newState.setType(selectedBlock.material());
        newState.update(true);
        superiorGenerator.islands().handleBlockPlace(newState.getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent event) {
        if (event.getBlock().getType() != Material.LAVA) return;

        Block target = event.getToBlock();
        if (target.getType().isSolid() || !isGeneratorTarget(target)) return;

        UUID islandUUID = superiorGenerator.islands()
                .islandAt(target.getLocation())
                .map(island -> island.uuid())
                .orElse(null);
        if (islandUUID == null) return;

        event.setCancelled(true);
        generateRandomBlock(target.getState(), islandUUID);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {
        UUID islandUUID = superiorGenerator.islands()
                .islandAt(event.getNewState().getLocation())
                .map(IslandService.IslandInfo::uuid)
                .orElse(null);
        if (islandUUID == null) return;

        boolean enableBasaltGen = superiorGenerator.getConfiguration().enableBasaltGenerator;
        if (event.getNewState().getType() == Material.LAVA) {
            event.setCancelled(true);
            Block relBlock = event.getBlock().getRelative(BlockFace.DOWN);
            if (relBlock.getType() == Material.WATER) {
                generateRandomBlock(relBlock.getState(), islandUUID);
            }
        } else if (event.getNewState().getType() == Material.BASALT && enableBasaltGen) {
            generateRandomBlock(event.getNewState(), islandUUID);
        }
    }

    private boolean isGeneratorTarget(Block target) {
        if (superiorGenerator.getConfiguration().enableBasaltGenerator
                && target.getRelative(BlockFace.DOWN).getType() == Material.SOUL_SOIL
                && hasNeighbor(target, Material.BLUE_ICE)) {
            return true;
        }

        for (BlockFace face : GENERATOR_FACES) {
            Block neighbor = target.getRelative(face);
            if (neighbor.getType() == Material.WATER) return true;
            if (neighbor.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) return true;
        }

        return false;
    }

    private boolean hasNeighbor(Block block, Material material) {
        for (BlockFace face : GENERATOR_FACES) {
            if (block.getRelative(face).getType() == material) return true;
        }
        return false;
    }
}

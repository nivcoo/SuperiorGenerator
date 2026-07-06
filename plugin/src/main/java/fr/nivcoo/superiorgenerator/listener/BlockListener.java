package fr.nivcoo.superiorgenerator.listener;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.hook.platform.SuperiorHook;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.GeneratorBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.UUID;

public class BlockListener implements Listener {

    private final SuperiorGenerator superiorGenerator;
    private final CacheManager cacheManager;
    private final GeneratorManager generatorManager;

    public BlockListener() {
        superiorGenerator = SuperiorGenerator.get();
        cacheManager = superiorGenerator.getCacheManager();
        generatorManager = superiorGenerator.getGeneratorManager();
    }

    private void generateRandomBlock(BlockState newState, UUID islandUUID) {
        AGenerator generator = cacheManager.getOrUpdateCurrentIslandGenerator(islandUUID);
        GeneratorBlock selectedBlock = generatorManager.getRandomBlock(generator);

        newState.setType(selectedBlock.material());
        newState.update(true);
        SuperiorHook.addBlockInIsland(newState.getBlock());
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        UUID islandUUID = SuperiorHook.getIslandUUIDByLocation(event.getNewState().getLocation());
        if (islandUUID == null) return;

        boolean enableBasaltGen = superiorGenerator.getConfiguration().enableBasaltGenerator;
        if (event.getNewState().getType() == Material.LAVA) {
            event.setCancelled(true);
            Block relBlock = event.getBlock().getRelative(BlockFace.DOWN);
            if (relBlock.getType() == Material.WATER) {
                generateRandomBlock(relBlock.getState(), islandUUID);
            }
        } else if (event.getNewState().getType() == Material.COBBLESTONE
                || (event.getNewState().getType() == Material.BASALT && enableBasaltGen)) {
            generateRandomBlock(event.getNewState(), islandUUID);
        }
    }
}

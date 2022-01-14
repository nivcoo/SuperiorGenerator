package fr.nivcoo.superiorgenerator.listener;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.hook.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.Generator;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.UUID;

public class BlockListener implements Listener {


    private SuperiorGenerator superiorGenerator;
    private Config config;
    private CacheManager cacheManager;
    private GeneratorManager generatorManager;

    public BlockListener() {
        superiorGenerator = SuperiorGenerator.get();

        config = superiorGenerator.getConfiguration();

        cacheManager = superiorGenerator.getCacheManager();
        generatorManager = superiorGenerator.getGeneratorManager();

    }


    private void generateRandomBlock(BlockState newState, UUID islandUUID) {

        Generator generator = cacheManager.getCurrentIslandGenerator(islandUUID);

        Material selectedBlock = generatorManager.getRandomBlock(generator);


        newState.setType(selectedBlock);

        SuperiorSkyblock2.addBlockInIsland(newState.getBlock());
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByLocation(event.getNewState().getLocation());
        if (islandUUID == null)
            return;

        boolean enableBasaltGen = config.getBoolean("enable_basalt_generator");
        if (event.getNewState().getType() == Material.LAVA) {
            event.setCancelled(true);
            Block relBlock = event.getBlock().getRelative(BlockFace.DOWN);
            if (relBlock.getType() == Material.WATER)
                generateRandomBlock(relBlock.getState(), islandUUID);
        } else if (event.getNewState().getType() == Material.COBBLESTONE ||
                (event.getNewState().getType() == Material.getMaterial("BASALT") && enableBasaltGen))
            generateRandomBlock(event.getNewState(), islandUUID);


    }

}

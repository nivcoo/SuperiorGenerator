package fr.nivcoo.superiorgenerator.messaging.action;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.core.messaging.BusAction;
import fr.nivcoo.utilsz.core.messaging.BusMessage;

import java.util.UUID;

@BusAction(value = "select", runOnMainThread = true)
public record SelectAction(UUID islandUUID, String generatorID) implements BusMessage {

    @Override
    public void execute() {
        SuperiorGenerator plugin = SuperiorGenerator.get();
        if (plugin == null) return;

        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();
        AGenerator generator = generatorManager.getGeneratorByID(generatorID);
        if (generator != null) {
            cacheManager.forceSelectGenerator(islandUUID, generator);
            plugin.getLogger().info("Generator " + generator.getID() + " sélectionné pour l'île " + islandUUID);
        }
    }
}

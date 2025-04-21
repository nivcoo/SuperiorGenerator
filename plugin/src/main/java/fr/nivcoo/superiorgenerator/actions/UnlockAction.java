package fr.nivcoo.superiorgenerator.actions;

import com.google.gson.JsonObject;
import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import fr.nivcoo.utilsz.redis.RedisAction;
import fr.nivcoo.utilsz.redis.RedisMessage;
import fr.nivcoo.utilsz.redis.RedisSerializable;

import java.util.UUID;

@RedisAction("unlock")
public record UnlockAction(UUID islandUUID, String generatorID) implements RedisSerializable {

    @Override
    public void execute() {
        SuperiorGenerator plugin = SuperiorGenerator.get();
        if (plugin == null) {
            return;
        }
        GeneratorManager generatorManager = plugin.getGeneratorManager();
        CacheManager cacheManager = plugin.getCacheManager();

        AGenerator generator = generatorManager.getGeneratorByID(generatorID);
        if (generator != null) {
            cacheManager.forceUnlockGenerator(islandUUID, generator);
            plugin.getLogger().info("Generator " + generator.getID() + " débloqué pour l'île " + islandUUID);
        }
    }
}

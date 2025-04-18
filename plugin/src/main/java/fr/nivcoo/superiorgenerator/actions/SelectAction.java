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

@RedisAction("select")
public record SelectAction(UUID islandUUID, String generatorID) implements RedisSerializable {

    public JsonObject toJson() {
        return new RedisMessage(getAction()).add("islandUUID", islandUUID).add("generatorID", generatorID).toJson();
    }

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
            cacheManager.forceSelectGenerator(islandUUID, generator);
            plugin.getLogger().info("Generator " + generator.getID() + " sélectionné pour l'île " + islandUUID);
        }

    }
}

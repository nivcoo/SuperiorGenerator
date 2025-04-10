package fr.nivcoo.superiorgenerator.redis;

import com.google.gson.JsonObject;
import fr.nivcoo.utilsz.redis.RedisMessage;
import fr.nivcoo.utilsz.redis.RedisSerializable;

import java.util.UUID;

public record GeneratorRedisMessage(UUID islandUUID, String generatorID, String action) implements RedisSerializable {

    public GeneratorRedisMessage(JsonObject json) {
        this(
                UUID.fromString(json.get("islandUUID").getAsString()),
                json.get("generatorID").getAsString(),
                json.get("action").getAsString()
        );
    }

    @Override
    public String getAction() {
        return action;
    }

    public JsonObject toJson() {
        return new RedisMessage(action)
                .add("islandUUID", islandUUID)
                .add("generatorID", generatorID)
                .toJson();
    }
}

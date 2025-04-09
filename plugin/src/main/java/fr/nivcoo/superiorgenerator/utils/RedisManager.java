package fr.nivcoo.superiorgenerator.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import org.bukkit.Bukkit;
import redis.clients.jedis.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisManager {

    private final JedisPool jedisPool;
    private final SuperiorGenerator plugin;
    private JedisPubSub pubSub;
    private Thread listenerThread;
    private volatile boolean running = true;

    public RedisManager(SuperiorGenerator plugin, String host, int port, String username, String password) {
        this.plugin = plugin;
        JedisClientConfig config;
        if ((password != null && !password.isEmpty()) || (username != null && !username.isEmpty())) {
            config = DefaultJedisClientConfig.builder()
                    .user(username == null || username.isEmpty() ? null : username)
                    .password(password == null || password.isEmpty() ? null : password)
                    .build();
        } else {
            config = DefaultJedisClientConfig.builder().build();
        }

        this.jedisPool = new JedisPool(new HostAndPort(host, port), config);
        startListener();
    }

    public void publishSelect(UUID islandUUID, String generatorID) {
        try (Jedis jedis = jedisPool.getResource()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", "select");
            obj.addProperty("islandUUID", islandUUID.toString());
            obj.addProperty("generatorID", generatorID);
            jedis.publish("superiorgenerator-update", obj.toString());
        }
    }

    public void publishUnlock(UUID islandUUID, String generatorID) {
        try (Jedis jedis = jedisPool.getResource()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", "unlock");
            obj.addProperty("islandUUID", islandUUID.toString());
            obj.addProperty("generatorID", generatorID);
            jedis.publish("superiorgenerator-update", obj.toString());
        }
    }

    private void startListener() {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (!channel.equals("superiorgenerator-update")) return;

                JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
                String action = obj.get("action").getAsString();
                UUID islandUUID = UUID.fromString(obj.get("islandUUID").getAsString());
                String generatorID = obj.get("generatorID").getAsString();

                AGenerator generator = plugin.getGeneratorManager().getGeneratorByID(generatorID);
                if (generator == null) return;

                switch (action) {
                    case "select" -> {
                        plugin.getCacheManager().forceSelectGenerator(islandUUID, generator);
                        System.out.println("Generator " + generator.getID() + " selected for island " + islandUUID);
                        plugin.getLog().info("Generator " + generator.getID() + " selected for island " + islandUUID);
                    }
                    case "unlock" -> {
                        plugin.getCacheManager().forceUnlockGenerator(islandUUID, generator);
                        plugin.getLog().info("Generator " + generator.getID() + " unlocked for island " + islandUUID);
                    }
                }
            }
        };

        listenerThread = new Thread(() -> {
            while (running) {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(pubSub, "superiorgenerator-update");
                } catch (Exception e) {
                    plugin.getLogger().warning("[Redis] Listener crashed: " + e.getMessage());

                    if (!running) break;

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    public void close() {
        running = false;
        try {
            if (pubSub != null) {
                pubSub.unsubscribe();
            }
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.join(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jedisPool.close();
    }
}

package fr.nivcoo.superiorgenerator.placeholder;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.hook.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.Generator;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceHolderAPI extends PlaceholderExpansion {

    private SuperiorGenerator superiorGenerator;

    public PlaceHolderAPI() {
        superiorGenerator = SuperiorGenerator.get();
    }

    @Override
    public @NotNull String getAuthor() {
        return superiorGenerator.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "superiorgenerator";
    }

    @Override
    public @NotNull String getVersion() {
        return superiorGenerator.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null)
            return "";
        CacheManager cacheManager = superiorGenerator.getCacheManager();

        GeneratorManager generatorManager = superiorGenerator.getGeneratorManager();
        if (identifier.equals("get_current_gen")) {
            return String.valueOf(cacheManager.getCurrentIslandGenerator(player).getID());
        }

        if (identifier.startsWith("is_unlocked_")) {
            UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(player);

            if (islandUUID == null)
                return "false";
            String generatorID = identifier.replace("is_unlocked_", "");
            Generator generator = generatorManager.getGeneratorByID(generatorID);
            if (generator == null)
                return "false";
            return String.valueOf(cacheManager.getIfUnlocked(islandUUID, generator));
        }

        return null;
    }


}
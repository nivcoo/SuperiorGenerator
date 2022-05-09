package fr.nivcoo.superiorgenerator.placeholder;

import com.bgsoftware.superiorskyblock.api.island.Island;
import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.hook.superiorskyblock.SuperiorSkyblock2;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
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
        } else if (identifier.equals("has_manage_permission")) {

            Island island = SuperiorSkyblock2.getIslandByMember(player);
            if (island == null)
                return "false";
            return String.valueOf(island.hasPermission(player, superiorGenerator.getSuperiorSkyblock2().getManageGeneratorPermission()));
        } else if (identifier.equals("get_current_category")) {


            return String.valueOf(cacheManager.getCurrentIslandGenerator(player).getCategory());
        } else if (identifier.startsWith("is_unlocked_")) {
            UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(player);

            if (islandUUID == null)
                return "false";
            String generatorID = identifier.replace("is_unlocked_", "");
            AGenerator generator = generatorManager.getGeneratorByID(generatorID);
            if (generator == null)
                return "false";
            return String.valueOf(cacheManager.isAlreadyUnlocked(islandUUID, generator));
        } else if (identifier.startsWith("unlocked_categories_number_")) {
            UUID islandUUID = SuperiorSkyblock2.getIslandUUIDByMember(player);
            if (islandUUID == null)
                return "0";
            String category = identifier.replace("unlocked_categories_number_", "");
            int number = cacheManager.getUnlockedCategoriesNumber(islandUUID, category);
            return String.valueOf(number);
        }

        return null;
    }


}

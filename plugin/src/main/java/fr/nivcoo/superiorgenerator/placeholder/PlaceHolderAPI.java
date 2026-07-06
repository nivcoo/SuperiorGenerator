package fr.nivcoo.superiorgenerator.placeholder;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.superiorgenerator.cache.CacheManager;
import fr.nivcoo.superiorgenerator.manager.GeneratorManager;
import fr.nivcoo.superiorgenerator.service.IslandService;
import fr.nivcoo.superiorgeneratorapi.manager.AGenerator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceHolderAPI extends PlaceholderExpansion {

    private final SuperiorGenerator superiorGenerator;

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
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null)
            return "";
        CacheManager cacheManager = superiorGenerator.getCacheManager();

        GeneratorManager generatorManager = superiorGenerator.getGeneratorManager();

        if (identifier.equals("get_current_gen")) {
            return String.valueOf(cacheManager.getOrUpdateCurrentIslandGenerator(player).getID());
        } else if (identifier.equals("has_manage_permission")) {

            return String.valueOf(superiorGenerator.islands()
                    .islandByMember(player)
                    .map(island -> superiorGenerator.islands().hasPermission(player, island.uuid(), IslandService.MANAGE_GENERATOR_PERMISSION))
                    .orElse(false));
        } else if (identifier.equals("get_current_category")) {


            return String.valueOf(cacheManager.getOrUpdateCurrentIslandGenerator(player).getCategory());
        } else if (identifier.startsWith("is_unlocked_")) {
            UUID islandUUID = superiorGenerator.islands()
                    .islandByMember(player)
                    .map(IslandService.IslandInfo::uuid)
                    .orElse(null);

            if (islandUUID == null)
                return "false";
            String generatorID = identifier.replace("is_unlocked_", "");
            AGenerator generator = generatorManager.getGeneratorByID(generatorID);
            if (generator == null)
                return "false";
            return String.valueOf(cacheManager.isAlreadyUnlocked(islandUUID, generator));
        } else if (identifier.startsWith("unlocked_categories_number_")) {
            UUID islandUUID = superiorGenerator.islands()
                    .islandByMember(player)
                    .map(IslandService.IslandInfo::uuid)
                    .orElse(null);
            if (islandUUID == null)
                return "0";
            String category = identifier.replace("unlocked_categories_number_", "");
            int number = cacheManager.getUnlockedCategoriesNumber(islandUUID, category);
            return String.valueOf(number);
        }

        return null;
    }


}

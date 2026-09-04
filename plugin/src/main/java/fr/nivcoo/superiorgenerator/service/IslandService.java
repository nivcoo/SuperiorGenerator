package fr.nivcoo.superiorgenerator.service;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class IslandService {

    public static final String MANAGE_GENERATOR_PERMISSION = "MANAGE_GENERATOR";

    private IslandResolver resolver = new IslandResolver() {
    };

    public void resolver(IslandResolver resolver) {
        this.resolver = resolver == null ? new IslandResolver() {
        } : resolver;
    }

    public Optional<IslandInfo> islandAt(Location location) {
        return resolver.islandAt(location);
    }

    public Optional<IslandInfo> islandByMember(Player player) {
        return resolver.islandByMember(player);
    }

    public boolean hasPermission(Player player, UUID islandUuid, String permission) {
        return resolver.hasPermission(player, islandUuid, permission);
    }

    public interface IslandResolver {
        default Optional<IslandInfo> islandAt(Location location) {
            return Optional.empty();
        }

        default Optional<IslandInfo> islandByMember(Player player) {
            return Optional.empty();
        }

        default boolean hasPermission(Player player, UUID islandUuid, String permission) {
            return false;
        }

    }

    public record IslandInfo(UUID uuid, boolean spawn) {
    }
}

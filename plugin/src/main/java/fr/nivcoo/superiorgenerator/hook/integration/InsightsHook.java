package fr.nivcoo.superiorgenerator.hook.integration;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import fr.nivcoo.superiorgenerator.hook.core.HookContext;
import fr.nivcoo.utilsz.platform.bukkit.hook.BukkitHook;
import fr.nivcoo.utilsz.platform.bukkit.tracking.BlockChangeProvider;
import fr.nivcoo.utilsz.platform.bukkit.tracking.BlockChangeService;
import java.util.LinkedHashMap;
import java.util.Map;

public final class InsightsHook implements BukkitHook<HookContext> {

    public InsightsHook(HookContext context) {
    }

    @Override
    public String id() {
        return "Insights";
    }

    @Override
    public String requiredPlugin() {
        return "Insights";
    }

    @Override
    public void load(HookContext context) {
        BlockChangeProvider provider = InsightsBlockChangeProvider.create();
        if (provider == null) {
            context.plugin().getLogger().warning("Insights is enabled but its API is unavailable; block tracking was not registered.");
            return;
        }
        context.plugin().blockChanges().register(provider);
    }
}

final class InsightsBlockChangeProvider extends InsightsListener implements BlockChangeProvider {

    static BlockChangeProvider create() {
        InsightsPlugin insights = InsightsPlugin.getInstance();
        return insights == null ? null : new InsightsBlockChangeProvider(insights);
    }

    private InsightsBlockChangeProvider(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "Insights";
    }

    @Override
    public void apply(BlockChangeService.Changes changes) {
        Map<ScanObject<?>, Long> deltas = new LinkedHashMap<>();
        changes.broken().forEach((material, amount) ->
                deltas.merge(ScanObject.of(material), -amount.longValue(), Long::sum));
        changes.placed().forEach((material, amount) ->
                deltas.merge(ScanObject.of(material), amount.longValue(), Long::sum));
        deltas.values().removeIf(delta -> delta == 0L);
        if (deltas.isEmpty()) return;

        handleModification(changes.location(), storage -> applyDeltas(storage, deltas));
    }

    private void applyDeltas(Storage storage, Map<ScanObject<?>, Long> deltas) {
        deltas.forEach(storage::modify);
    }
}

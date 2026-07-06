package fr.nivcoo.superiorgenerator.hook.core;

import fr.nivcoo.superiorgenerator.SuperiorGenerator;
import fr.nivcoo.utilsz.platform.bukkit.hook.BukkitHookContext;

public final class HookContext extends BukkitHookContext {

    private final SuperiorGenerator plugin;

    public HookContext(SuperiorGenerator plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public SuperiorGenerator plugin() {
        return plugin;
    }
}

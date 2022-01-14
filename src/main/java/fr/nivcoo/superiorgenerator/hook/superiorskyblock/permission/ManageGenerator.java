package fr.nivcoo.superiorgenerator.hook.superiorskyblock.permission;

import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ManageGenerator implements Listener {

    private static IslandPrivilege MANAGE_GENERATOR;

    @EventHandler
    public void init(PluginInitializeEvent e) {
        IslandPrivilege.register("MANAGE_GENERATOR");
        MANAGE_GENERATOR = IslandPrivilege.getByName("MANAGE_GENERATOR");
    }

}

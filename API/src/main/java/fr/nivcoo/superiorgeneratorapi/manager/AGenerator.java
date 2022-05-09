package fr.nivcoo.superiorgeneratorapi.manager;

import fr.nivcoo.utilsz.config.Pair;
import org.bukkit.Material;

import java.util.HashMap;

public interface AGenerator {

    String getID();

    String getCategory();

    HashMap<Pair<Material, Byte>, Double> getBlocks();
}

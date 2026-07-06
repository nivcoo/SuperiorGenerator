package fr.nivcoo.superiorgeneratorapi.manager;

import java.util.List;

public interface AGenerator {

    String getID();

    String getCategory();

    List<GeneratorBlock> getBlocks();
}

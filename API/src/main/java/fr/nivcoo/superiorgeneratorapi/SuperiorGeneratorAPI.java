package fr.nivcoo.superiorgeneratorapi;


import fr.nivcoo.superiorgeneratorapi.manager.AGeneratorManager;

public class SuperiorGeneratorAPI {

    private static ASuperiorGenerator instance;

    public static ASuperiorGenerator getSuperiorGenerator() {
        return instance;
    }

    public AGeneratorManager getGeneratorManager() {
        return instance.getGeneratorManager();
    }

}

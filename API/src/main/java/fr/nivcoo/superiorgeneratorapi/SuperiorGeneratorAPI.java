package fr.nivcoo.superiorgeneratorapi;


public final class SuperiorGeneratorAPI {

    private static ASuperiorGenerator api;

    private SuperiorGeneratorAPI() {
    }

    public static ASuperiorGenerator get() {
        return api;
    }

    public static ASuperiorGenerator getSuperiorGenerator() {
        return get();
    }

    public static void set(ASuperiorGenerator api) {
        SuperiorGeneratorAPI.api = api;
    }

}

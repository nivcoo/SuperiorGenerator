package fr.nivcoo.superiorgenerator.config;

import fr.nivcoo.utilsz.core.config.annotations.Section;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GeneratorsConfig {

    public Map<String, GeneratorConfig> generators = new LinkedHashMap<>();

    public GeneratorsConfig() {
        generators.put("default", new GeneratorConfig("1", List.of(
                "COBBLESTONE:80",
                "STONE:15",
                "DIORITE:5"
        )));
        generators.put("bedrock", new GeneratorConfig("2", List.of(
                "BEDROCK:100"
        )));
    }

    @Section
    public static final class GeneratorConfig {
        public String category = "1";
        public List<String> blocks = List.of("COBBLESTONE:100");

        public GeneratorConfig() {
        }

        public GeneratorConfig(String category, List<String> blocks) {
            this.category = category;
            this.blocks = blocks;
        }
    }
}

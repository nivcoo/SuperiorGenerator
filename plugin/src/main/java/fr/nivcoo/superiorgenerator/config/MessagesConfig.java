package fr.nivcoo.superiorgenerator.config;

import fr.nivcoo.utilsz.core.config.ConfigManager;
import fr.nivcoo.utilsz.core.config.annotations.Section;
import net.kyori.adventure.text.Component;

import java.util.List;

public final class MessagesConfig {

    public Commands commands = new Commands();

    @Section
    public static final class Commands {
        public Component incorrectUsage = ConfigManager.parseDynamic("&7[&c&lES&7] Vous devez faire : &b/{0}");
        public Component noPermission = ConfigManager.parseDynamic("&7[&c&lES&7] &cCommande inconnue.");
        public List<Component> help = List.of(
                ConfigManager.parseDynamic("&7&m------------------&8[&6Générateurs&8]&7&m------------------"),
                ConfigManager.parseDynamic("{!superiorgenerator.command.unlock}&6/generator unlock &eDébloquer un générateur !"),
                ConfigManager.parseDynamic("{!superiorgenerator.command.select}&6/generator select &eSélectionner un générateur !"),
                ConfigManager.parseDynamic("&7&m----------------------------------------------")
        );
        public Unlock unlock = new Unlock();
        public Select select = new Select();
    }

    @Section
    public static final class Unlock {
        public Component success = ConfigManager.parseDynamic("&7[&c&lES&7] &aLe générateur &b{generator} &aa été débloqué pour &b{player} &a!");
        public Component notFound = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe générateur {generator} n'a pas été trouvé !");
        public Component notFoundPlayer = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe joueur {player} n'a pas été trouvé !");
        public Component alreadyUnlock = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe générateur {generator} est déjà débloqué pour {player} !");
        public Component noIsland = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe joueur &b{player} &cn'a pas d'île !");
    }

    @Section
    public static final class Select {
        public Component success = ConfigManager.parseDynamic("&7[&c&lES&7] Le générateur a été changé avec succès !");
        public Component alreadySelected = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe générateur sélectionné est déjà celui mis en place !");
        public Component notUnlocked = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe générateur n'est pas débloqué !");
        public Component noPermission = ConfigManager.parseDynamic("&7[&c&lES&7] &cVous ne pouvez pas améliorer votre générateur !");
        public Component noIsland = ConfigManager.parseDynamic("&7[&c&lES&7] &cVous n'avez pas d'île !");
        public Other other = new Other();
    }

    @Section
    public static final class Other {
        public Component notFoundPlayer = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe joueur {player} n'a pas été trouvé !");
        public Component noIsland = ConfigManager.parseDynamic("&7[&c&lES&7] &cLe joueur n'a pas d'île !");
    }
}

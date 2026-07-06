package fr.nivcoo.superiorgenerator.config;

import fr.nivcoo.utilsz.core.config.common.DatabaseConfig;
import fr.nivcoo.utilsz.core.config.common.MessagingConfig;

public final class MainConfig {
    public DatabaseConfig database = new DatabaseConfig("sqlite", "database.db", "superior_generator", "root");
    public MessagingConfig messaging = new MessagingConfig(false, "superiorgenerator");
    public boolean enableBasaltGenerator = false;
}

package me.grabsky.claims.configuration;

import me.grabsky.claims.Claims;
import me.grabsky.indigo.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private final Claims instance;
    private final ConsoleLogger consoleLogger;
    private final File file;

    public static boolean LOGS;
    public static World DEFAULT_WORLD;
    public static String REGION_PREFIX;
    public static int REGION_PRIORITY;
    public static int TELEPORT_DELAY;
    public static int MEMBERS_LIMIT;
    public static int MINIMUM_DISTANCE_FROM_SPAWN;

    public static String LOG_FORMAT_PLACED;
    public static String LOG_FORMAT_DESTROYED;
    public static String LOG_FORMAT_UPGRADED;
    public static String LOG_FORMAT_MEMBER_ADDED;
    public static String LOG_FORMAT_MEMBER_REMOVED;

    public Config(Claims instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "config.yml");
    }

    public void reload() {
        // Saving default config
        if(!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        final FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != 1) {
            consoleLogger.error("Your config.yml file is outdated. Plugin may not work properly.");
        }
        LOGS = fc.getBoolean("settings.logs");
        // Getting the default world (or disabling plugin if value is not present)
        final String defaultWorldName = fc.getString("settings.claims-world");
        if (defaultWorldName == null || Bukkit.getWorld(defaultWorldName) == null) {
            consoleLogger.error("Config file is missing name of your claims-world. Plugin will be disabled to prevent unexpected behaviour.");
            instance.getPluginLoader().disablePlugin(instance);
            return;
        }
        DEFAULT_WORLD = Bukkit.getWorld(defaultWorldName);
        REGION_PREFIX = fc.getString("settings.region-prefix");
        REGION_PRIORITY = fc.getInt("settings.region-priority");
        // Overriding other values...
        TELEPORT_DELAY = fc.getInt("settings.claim.teleport-delay");
        MEMBERS_LIMIT = fc.getInt("settings.claim.members-limit");
        MINIMUM_DISTANCE_FROM_SPAWN = fc.getInt("settings.claim.minimum-distance-from-spawn");
        // Logging formats
        LOG_FORMAT_PLACED = fc.getString("settings.logging-format.claim-placed");
        LOG_FORMAT_DESTROYED = fc.getString("settings.logging-format.claim-destroyed");
        LOG_FORMAT_UPGRADED = fc.getString("settings.logging-format.claim-upgraded");
        LOG_FORMAT_MEMBER_ADDED = fc.getString("settings.logging-format.claim-member-added");
        LOG_FORMAT_MEMBER_REMOVED = fc.getString("settings.logging-format.claim-member-removed");
    }
}

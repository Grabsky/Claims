package net.skydistrict.claims.configuration;

import net.skydistrict.claims.Claims;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    private static final Claims instance = Claims.getInstance();
    private static final int CONFIG_VERSION = 1;

    public static String REGION_PREFIX;
    public static World DEFAULT_WORLD;
    public static int TELEPORT_DELAY;
    public static int MEMBERS_LIMIT;
    public static int MINIMUM_DISTANCE_FROM_SPAWN;

    public static void reload() {
        // Saving default config
        File file = new File(instance.getDataFolder() + "/config.yml");
        if(!file.exists()) {
            instance.saveResource("config.yml", false);
        }
        // Overriding...
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != CONFIG_VERSION) {
            instance.getLogger().warning("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        REGION_PREFIX = fc.getString("settings.region-prefix");
        // Getting the default world (or disabling plugin if value is not present)
        final String defaultWorldName = fc.getString("settings.claims-world");
        if (defaultWorldName == null || Bukkit.getWorld(defaultWorldName) == null) {
            instance.getLogger().warning(ChatColor.RED + "Config file is missing name of your claims-world. Plugin will be disabled to prevent unexpected behaviour.");
            instance.getPluginLoader().disablePlugin(instance);
            return;
        }
        DEFAULT_WORLD = Bukkit.getWorld(defaultWorldName);
        // Overriding other values...
        TELEPORT_DELAY = fc.getInt("settings.claims.teleport-delay");
        MEMBERS_LIMIT = fc.getInt("settings.claims.members-limit");
        MINIMUM_DISTANCE_FROM_SPAWN = fc.getInt("settings.claims.minimum-distance-from-spawn");
    }
}

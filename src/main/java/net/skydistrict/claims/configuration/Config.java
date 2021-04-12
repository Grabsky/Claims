package net.skydistrict.claims.configuration;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Config {
    public static World DEFAULT_WORLD = Bukkit.getWorlds().get(0);
    public static int MEMBERS_LIMIT = 10;
    public static int MIN_DISTANCE_FROM_SPAWN = 300;
}

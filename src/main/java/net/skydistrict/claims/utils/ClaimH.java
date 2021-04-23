package net.skydistrict.claims.utils;

import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ClaimH {
    private static final ClaimLevel[] levels = new ClaimLevel[5];

    public static void initialize() {
        levels[0] = new ClaimLevel("Węgiel", "31x31", ChatColor.DARK_GRAY, Material.COAL_BLOCK, Material.COAL, Items.COAL_BLOCK);
        levels[1] = new ClaimLevel("Żelazo", "41x41", ChatColor.WHITE,  Material.IRON_BLOCK, Material.IRON_INGOT, Items.IRON_BLOCK);
        levels[2] = new ClaimLevel("Złoto", "51x51", ChatColor.GOLD,  Material.GOLD_BLOCK, Material.GOLD_INGOT, Items.GOLD_BLOCK);
        levels[3] = new ClaimLevel("Diament", "61x61", ChatColor.AQUA, Material.DIAMOND_BLOCK, Material.DIAMOND, Items.DIAMOND_BLOCK);
        levels[4] = new ClaimLevel("Szmaragd", "71x71", ChatColor.GREEN, Material.EMERALD_BLOCK, Material.EMERALD, Items.EMERALD_BLOCK);
    }

    /**
     * Returns ClaimLevel for given level (numeric)
     */
    public static ClaimLevel getClaimLevel(int level) {
        return levels[level];
    }

    /**
     * Creates region ID from org.bukkit.Location
     */
    public static String createId(org.bukkit.Location location) {
        return new StringBuilder().append(Config.REGION_PREFIX).append("x").append(location.getBlockX()).append("y").append(location.getBlockY()).append("z").append(location.getBlockZ()).toString();
    }
}

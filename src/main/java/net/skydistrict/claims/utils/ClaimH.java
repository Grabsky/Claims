package net.skydistrict.claims.utils;

import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.configuration.StaticItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ClaimH {
    private static final List<ClaimLevel> levels = new ArrayList<>(5);

    public static void initialize() {
        levels.add(new ClaimLevel("Węgiel", "31x31", ChatColor.DARK_GRAY, Material.COAL_BLOCK, Material.COAL, StaticItems.COAL_BLOCK));
        levels.add(new ClaimLevel("Żelazo", "41x41", ChatColor.WHITE,  Material.IRON_BLOCK, Material.IRON_INGOT, StaticItems.IRON_BLOCK));
        levels.add(new ClaimLevel("Złoto", "51x51", ChatColor.GOLD,  Material.GOLD_BLOCK, Material.GOLD_INGOT, StaticItems.GOLD_BLOCK));
        levels.add(new ClaimLevel("Diament", "61x61", ChatColor.AQUA, Material.DIAMOND_BLOCK, Material.DIAMOND, StaticItems.DIAMOND_BLOCK));
        levels.add(new ClaimLevel("Szmaragd", "71x71", ChatColor.GREEN, Material.EMERALD_BLOCK, Material.EMERALD, StaticItems.EMERALD_BLOCK));
    }

    /** Returns ClaimLevel for given level (numeric) */
    public static ClaimLevel getClaimLevel(int level) {
        return levels.get(level);
    }

    /** Creates region ID from com.sk89q.worldedit.util.Location */
    public static String createId(com.sk89q.worldedit.util.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    /** Creates region ID from org.bukkit.Location */
    public static String createId(org.bukkit.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}

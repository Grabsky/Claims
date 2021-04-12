package net.skydistrict.claims.utils;

import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.configuration.StaticItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class UpgradeH {
    private static final List<ClaimLevel> levels = new ArrayList<>(5);

    public static void initialize() {
        levels.add(new ClaimLevel("Węgiel", ChatColor.DARK_GRAY, "31x31", Material.COAL, StaticItems.COAL_BLOCK));
        levels.add(new ClaimLevel("Żelazo", ChatColor.WHITE, "41x41", Material.IRON_INGOT, StaticItems.IRON_BLOCK));
        levels.add(new ClaimLevel("Złoto", ChatColor.GOLD, "51x51", Material.GOLD_INGOT, StaticItems.GOLD_BLOCK));
        levels.add(new ClaimLevel("Diament", ChatColor.AQUA, "61x61", Material.DIAMOND, StaticItems.DIAMOND_BLOCK));
        levels.add(new ClaimLevel("Szmaragd", ChatColor.GREEN, "71x71",  Material.EMERALD, StaticItems.EMERALD_BLOCK));
    }

    public static ClaimLevel getClaimLevel(int level) {
        return levels.get(level);
    }
}

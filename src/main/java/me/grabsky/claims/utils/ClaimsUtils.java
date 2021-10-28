package me.grabsky.claims.utils;

import me.grabsky.claims.claims.ClaimLevel;
import me.grabsky.claims.templates.Icons;
import me.grabsky.claims.templates.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ClaimsUtils {
    private static final ClaimLevel[] levels = new ClaimLevel[6];
    private static final ClaimLevel COAL = new ClaimLevel("Węgiel", "31x31", "§7", Material.COAL_BLOCK, Icons.LEVEL_COAL);
    private static final ClaimLevel IRON = new ClaimLevel("Żelazo", "41x41", "§f", Material.IRON_BLOCK, Icons.LEVEL_IRON)
            .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.IRON_INGOT, 64));
    private static final ClaimLevel GOLD = new ClaimLevel("Złoto", "51x51", "§e", Material.GOLD_BLOCK, Icons.LEVEL_GOLD)
            .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.GOLD_INGOT, 48));
    private static final ClaimLevel DIAMOND = new ClaimLevel("Diament", "61x61", "§b", Material.DIAMOND_BLOCK, Icons.LEVEL_DIAMOND)
            .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.DIAMOND, 32));
    private static final ClaimLevel EMERALD = new ClaimLevel("Szmaragd", "71x71", "§a", Material.EMERALD_BLOCK, Icons.LEVEL_EMERALD)
            .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.EMERALD, 16));
    private static final ClaimLevel NETHERITE = new ClaimLevel("Netheryt", "81x81", "§8", Material.NETHERITE_BLOCK, Icons.LEVEL_NETHERITE)
            .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.NETHERITE_INGOT, 8));

    // Returns ClaimLevel for given level (numeric) (defaults to COAL if provided level is invalid)
    public static ClaimLevel getClaimLevel(int level) {
        return switch(level) {
            case 1 -> IRON;
            case 2 -> GOLD;
            case 3 -> DIAMOND;
            case 4 -> EMERALD;
            case 5 -> NETHERITE;
            default -> COAL;
        };
    }
}

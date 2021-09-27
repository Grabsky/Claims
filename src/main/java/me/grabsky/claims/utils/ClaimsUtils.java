package me.grabsky.claims.utils;

import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import me.grabsky.claims.claims.ClaimLevel;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.templates.Icons;
import me.grabsky.claims.templates.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ClaimsUtils {
    private static final ClaimLevel[] levels = new ClaimLevel[6];

    public static void initialize() {
        // Default level - does not require anything to upgrade
        levels[0] = new ClaimLevel("Węgiel", "31x31", "§7", Material.COAL_BLOCK, Icons.LEVEL_COAL);
        // Upgradeable levels
        levels[1] = new ClaimLevel("Żelazo", "41x41", "§f", Material.IRON_BLOCK, Icons.LEVEL_IRON)
                .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.IRON_INGOT, 64));
        levels[2] = new ClaimLevel("Złoto", "51x51", "§e", Material.GOLD_BLOCK, Icons.LEVEL_GOLD)
                .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.GOLD_INGOT, 48));
        levels[3] = new ClaimLevel("Diament", "61x61", "§b", Material.DIAMOND_BLOCK, Icons.LEVEL_DIAMOND)
                .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.DIAMOND, 32));
        levels[4] = new ClaimLevel("Szmaragd", "71x71", "§a", Material.EMERALD_BLOCK, Icons.LEVEL_EMERALD)
                .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.EMERALD, 16));
        levels[5] = new ClaimLevel("Netheryt", "81x81", "§8", Material.NETHERITE_BLOCK, Icons.LEVEL_NETHERITE)
                .addUpgradeItems(Items.UPGRADE_CRYSTAL, new ItemStack(Material.NETHERITE_INGOT, 8));
    }

    // Returns ClaimLevel for given level (numeric)
    public static ClaimLevel getClaimLevel(int level) {
        return levels[level];
    }

    // Creates region ID from org.bukkit.Location
    public static String createId(org.bukkit.Location location) {
        return new StringBuilder().append(ClaimsConfig.REGION_PREFIX).append("x").append(location.getBlockX()).append("y").append(location.getBlockY()).append("z").append(location.getBlockZ()).toString();
    }

    // Returns list of options (flag values)
    public static List<Object> getFlagOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) return Arrays.asList(StateFlag.State.ALLOW, StateFlag.State.DENY);
        // Comparing instances is useless in some cases - let's check the name instead
        return switch (flagType.getName()) {
            case "weather-lock" -> Arrays.asList(null, WeatherTypes.CLEAR, WeatherTypes.RAIN, WeatherTypes.THUNDER_STORM);
            case "time-lock" -> Arrays.asList(null, "0", "6000", "12000", "18000");
            default -> null;
        };
    }

    // Returns list of options (formatted flag values) displayed in GUI
    public static List<String> getFormattedFlagOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) return Arrays.asList("Włączone", "Wyłączone");
        // Comparing instances is pointless in some cases - let's check the name instead
        return switch (flagType.getName()) {
            case "weather-lock" -> Arrays.asList("Wyłączone", "Słonecznie", "Deszcz", "Burza");
            case "time-lock" -> Arrays.asList("Wyłączone", "Rano (6:00)", "Południe (12:00)", "Wieczór (18:00)", "Północ (00:00)");
            default -> null;
        };
    }
}

package net.skydistrict.claimsgui.utils;

import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgradeH {

    private static final List<String> order = new ArrayList<>(Arrays.asList("COAL", "IRON", "GOLD", "DIAMOND", "EMERALD"));

    public static String getNextLevelAlias(String alias) {
        return order.get(order.indexOf(alias) + 1);
    }

    /**
     * Returns translated name of specific region type
     */
    public static String translate(String alias) {
        switch (alias) {
            case "COAL":
                return "Węgiel";
            case "IRON":
                return "Żelazo";
            case "GOLD":
                return "Złoto";
            case "DIAMOND":
                return "Diament";
            case "EMERALD":
                return "Szmaragd";
        }
        return "";
    }

    /**
     * Returns material color for specific region type
     */
    public static ChatColor color(String alias) {
        switch (alias) {
            case "COAL":
                return ChatColor.DARK_GRAY;
            case "IRON":
                return ChatColor.WHITE;
            case "GOLD":
                return ChatColor.GOLD;
            case "DIAMOND":
                return ChatColor.AQUA;
            case "EMERALD":
                return ChatColor.GREEN;
        }
        return null;
    }

    /**
     * Returns size of given region
     */
    public static int getSize(String alias) {
        switch (alias) {
            case "COAL":
                return 15;
            case "IRON":
                return 20;
            case "GOLD":
                return 25;
            case "DIAMOND":
                return 30;
            case "EMERALD":
                return 35;
        }
        return 0;
    }

    /**
     * Returns size of given region
     */
    public static Material getUpgradeMaterial(String alias) {
        switch (alias) {
            case "IRON":
                return Material.IRON_INGOT;
            case "GOLD":
                return Material.GOLD_INGOT;
            case "DIAMOND":
                return Material.DIAMOND;
            case "EMERALD":
                return Material.EMERALD;
        }
        return null;
    }

    public static ItemBuilder getBuilder(String alias) {
        switch (alias) {
            case "COAL":
                return StaticItems.COAL_BLOCK;
            case "IRON":
                return StaticItems.IRON_BLOCK;
            case "GOLD":
                return StaticItems.GOLD_BLOCK;
            case "DIAMOND":
                return StaticItems.DIAMOND_BLOCK;
            case "EMERALD":
                return StaticItems.EMERALD_BLOCK;
            default:
                return null;
        }
    }
}

package net.skydistrict.claimsgui.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Upgrade {

    private static List<String> order = new ArrayList<String>(Arrays.asList("COAL", "IRON", "GOLD", "DIAMOND", "EMERALD"));

    public static String getNextLevelAlias(String alias) {
        return order.get(order.indexOf(alias) + 1);
    }

    public static String translate(String type) {
        switch (type) {
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

    public static ChatColor color(String type) {
        switch (type) {
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

    /** Returns size of given region */
    public static int getSize(String type) {
        switch (type) {
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

    /** Returns size of given region */
    public static ItemStack getUpgradePrice(String type) {
        switch (type) {
            case "IRON":
                return new ItemStack(Material.IRON_INGOT, 64);
            case "GOLD":
                return new ItemStack(Material.GOLD_INGOT, 64);
            case "DIAMOND":
                return new ItemStack(Material.DIAMOND, 64);
            case "EMERALD":
                return new ItemStack(Material.EMERALD, 64);
        }
        return null;
    }
}

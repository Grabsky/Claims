package net.skydistrict.claimsgui.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Upgrade {

    public static List<String> ORDER = new ArrayList<String>(Arrays.asList("COAL_BLOCK", "IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK"));

    public static String translate(String type) {
        switch (type) {
            case "COAL_BLOCK":
                return "Węgiel";
            case "IRON_BLOCK":
                return "Żelazo";
            case "GOLD_BLOCK":
                return "Złoto";
            case "DIAMOND_BLOCK":
                return "Diament";
            case "EMERALD_BLOCK":
                return "Szmaragd";
        }
        return "";
    }

    public static ChatColor color(String type) {
        switch (type) {
            case "COAL_BLOCK":
                return ChatColor.DARK_GRAY;
            case "IRON_BLOCK":
                return ChatColor.WHITE;
            case "GOLD_BLOCK":
                return ChatColor.GOLD;
            case "DIAMOND_BLOCK":
                return ChatColor.AQUA;
            case "EMERALD_BLOCK":
                return ChatColor.GREEN;
        }
        return null;
    }

    /** Returns size of given region */
    public static int getSize(String type) {
        switch (type) {
            case "COAL_BLOCK":
                return 15;
            case "IRON_BLOCK":
                return 20;
            case "GOLD_BLOCK":
                return 25;
            case "DIAMOND_BLOCK":
                return 30;
            case "EMERALD_BLOCK":
                return 35;
        }
        return 0;
    }

    /** Returns size of given region */
    public static ItemStack getUpgradePrice(String type) {
        switch (type) {
            case "COAL_BLOCK":
                return new ItemStack(Material.IRON_INGOT, 64);
            case "IRON_BLOCK":
                return new ItemStack(Material.GOLD_INGOT, 64);
            case "GOLD_BLOCK":
                return new ItemStack(Material.DIAMOND, 64);
            case "DIAMOND_BLOCK":
                return new ItemStack(Material.EMERALD, 64);
        }
        return null;
    }
}

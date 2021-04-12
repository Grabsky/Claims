package net.skydistrict.claims.utils;

import org.bukkit.ChatColor;

public class TextH {
    /** Returns formatted string */
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}

package net.skydistrict.claimsgui.utils;

import org.bukkit.ChatColor;

public class TextUtils {
    /** Returns formatted string */
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}

package net.skydistrict.claims.utils;

public class ClaimH {
    /** Creates region ID from com.sk89q.worldedit.util.Location */
    public static String createId(com.sk89q.worldedit.util.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    /** Creates region ID from org.bukkit.Location */
    public static String createId(org.bukkit.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}

package net.skydistrict.claims.utils;

public class ClaimH {

    public static String createId(com.sk89q.worldedit.util.Location location) {
        return "claims_" + location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockY();
    }

    public static String createId(org.bukkit.Location location) {
        return "claims_" + location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockY();
    }
}

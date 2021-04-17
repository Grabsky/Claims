package net.skydistrict.claims.utils;

import com.sk89q.worldguard.protection.managers.RegionManager;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.ClaimCache;
import org.bukkit.Location;

public class ClaimH {
    private static final RegionManager manager = Claims.getInstance().getRegionManager();
    /** Creates region ID from com.sk89q.worldedit.util.Location */
    public static String createId(com.sk89q.worldedit.util.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    /** Creates region ID from org.bukkit.Location */
    public static String createId(org.bukkit.Location location) {
        return "claims_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    /** Returns true if player can place a claim at given location */
    public static boolean canPlaceAt(Location location) {
        Location center = ClaimCache.getClosestTo(location);
        if (center == null) return true;
        return (Math.abs(location.getBlockX() - center.getBlockX()) > 70 || Math.abs(location.getBlockZ() - center.getBlockZ()) > 70);
    }
}

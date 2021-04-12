package net.skydistrict.claims.claims;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.skydistrict.claims.Claims;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TO-DO: This must be implemented in a reasonable way.
// We still have to decide whether we should use this or more OOP-friendly approach.
public class ClaimCache {
    private final Claims instance;
    private final RegionManager regionManager;
    private static final Map<String, Claim> idToClaim = new HashMap<>();
    private static final Map<UUID, Claim> uuidToClaim = new HashMap<>();

    public ClaimCache(Claims instance) {
        this.instance = instance;
        this.regionManager = instance.getRegionManager();
        this.cacheClaims();
    }

    // Recommended to be ran only during the server startup
    private void cacheClaims() {
        for (Map.Entry<String, ProtectedRegion> en : regionManager.getRegions().entrySet()) {
            ProtectedRegion region = en.getValue();
            if (region.getId().startsWith("claims_")) {
                if (region.hasMembersOrOwners()) {
                    if (region.getOwners().size() == 1) {
                        String id = region.getId();
                        UUID owner = region.getOwners().getUniqueIds().iterator().next();
                        idToClaim.put(id, new Claim(region.getId(), region, owner));
                        System.out.println("Loaded claim owned by " + owner);
                    }
                }
            }
        }
    }

    public static boolean exists(String id) {
        return idToClaim.containsKey(id);
    }

    public static boolean exists(UUID uuid) {
        return uuidToClaim.containsKey(uuid);
    }

    public static void add(String id, Claim claim) {
        idToClaim.put(id, claim);
    }

    public static void add(UUID uuid, Claim claim) {
        uuidToClaim.put(uuid, claim);
    }

    public static Claim get(UUID uuid) {
        return uuidToClaim.get(uuid);
    }

    public static Claim get(String id) {
        return idToClaim.get(id);
    }

}

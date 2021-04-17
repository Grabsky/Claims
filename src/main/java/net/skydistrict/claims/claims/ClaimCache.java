package net.skydistrict.claims.claims;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.skydistrict.claims.Claims;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TO-DO: This  must be implemented in a reasonable way.
// We still have to decide whether we should use this or more OOP-friendly approach.
// Solution I currently have in mind is to map ONLY players that own a region, or are added to a region as a member.
// If that won't work - we can loop through claims and see if player is on member list. Just make it async CompletableFuture.
public class ClaimCache {
    private final Claims instance;
    private final RegionManager regionManager;
    private static final Map<String, Claim> regionIdToClaim = new HashMap<>();
    private static final Map<UUID, ClaimPlayer> uuidToClaimPlayer = new HashMap<>();
    private static final Map<String, Location> centers = new HashMap<>();

    public ClaimCache(Claims instance) {
        this.instance = instance;
        this.regionManager = instance.getRegionManager();
        this.cachePlayers();
        this.cacheClaims();
    }

    public void cachePlayers() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            UUID uuid = offlinePlayer.getUniqueId();
            uuidToClaimPlayer.put(uuid, new ClaimPlayer(uuid));
        }
    }

    // Recommended to be ran only during the server startup
    private void cacheClaims() {
        for (Map.Entry<String, ProtectedRegion> en : regionManager.getRegions().entrySet()) {
            ProtectedRegion region = en.getValue();
            if (region.getId().startsWith("claims_")) {
                if (region.hasMembersOrOwners()) {
                    if (region.getOwners().size() == 1) {
                        UUID owner = region.getOwners().getUniqueIds().iterator().next();
                        ClaimPlayer cp = uuidToClaimPlayer.get(owner);
                        Claim claim = new Claim(region.getId(), region, owner);
                        cp.setClaim(claim);
                        // Creating members' references
                        for (UUID memberUuid : region.getMembers().getUniqueIds()) {
                            ClaimPlayer cpm = uuidToClaimPlayer.get(memberUuid);
                            cpm.addRelative(claim.getId());
                        }
                        // Adding claim to the cache
                        String id = region.getId();
                        addClaim(id, claim);
                        System.out.println("Loaded claim owned by " + owner);
                    }
                }
            }
        }
    }

    // Claims
    public static boolean containsClaim(String id) {
        return regionIdToClaim.containsKey(id);
    }

    public static Claim getClaim(String id) {
        return regionIdToClaim.get(id);
    }

    public static void addClaim(String id, Claim claim) {
        regionIdToClaim.put(id, claim);
        centers.put(id, claim.getCenter());
    }

    public static void removeClaim(String id) {
        regionIdToClaim.remove(id);
        centers.remove(id);
    }

    // ClaimPlayers
    public static boolean containsClaimPlayer(UUID uuid) {
        return uuidToClaimPlayer.containsKey(uuid);
    }

    public static ClaimPlayer getClaimPlayer(UUID uuid) {
        return uuidToClaimPlayer.get(uuid);
    }

    public static void addClaimPlayer(UUID uuid) {
        uuidToClaimPlayer.put(uuid, new ClaimPlayer(uuid));
    }

    public static void removeClaimPlayer(UUID uuid) {
        uuidToClaimPlayer.remove(uuid);
    }

    @Nullable
    public static Location getClosestTo(Location location) {
        Location closestLocation = null;
        double dist = Double.MAX_VALUE;
        for (Location loc : centers.values()) {
            double d = loc.distance(location);
            if (d < dist) {
                closestLocation = loc;
                dist = d;
            }
        }
        return closestLocation;
    }
}

package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.api.ClaimsAPI;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLang;
import cloud.grabsky.claims.flags.ExtraFlags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.libs.paperlib.PaperLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimManager implements ClaimsAPI {
    private final RegionManager regionManager;
    private final ConsoleLogger consoleLogger;
    private final Map<String, Claim> regionIdToClaim = new HashMap<>();
    private final Map<UUID, ClaimPlayer> uuidToClaimPlayer = new HashMap<>();
    private final Map<String, Location> centers = new HashMap<>();

    public ClaimManager(Claims instance) {
        this.regionManager = instance.getRegionManager();
        this.consoleLogger = instance.getConsoleLogger();
    }

    // Should be run only during the server startup
    public void cacheClaims() {
        int loadedClaims = 0;
        for (Map.Entry<String, ProtectedRegion> en : regionManager.getRegions().entrySet()) {
            final ProtectedRegion region = en.getValue();
            if (!region.getId().startsWith(ClaimsConfig.REGION_PREFIX) || !region.hasMembersOrOwners() || region.getOwners().size() != 1) continue;
            final UUID owner = region.getOwners().getUniqueIds().iterator().next();
            final ClaimPlayer cp = this.getClaimPlayer(owner);
            final Claim claim = new Claim(region.getId(), owner, region);
            cp.setClaim(claim);
            // Creating members' references
            for (UUID memberUuid : region.getMembers().getUniqueIds()) {
                ClaimPlayer cm = this.getClaimPlayer(memberUuid);
                cm.addRelative(claim.getId());
            }
            // Adding claim to the cache
            final String id = region.getId();
            this.addClaim(id, claim);
            loadedClaims++;
        }
        consoleLogger.success("Loaded " + loadedClaims + " claims.");
    }

    // Returns true if Claim is in cache
    public boolean containsClaim(String id) {
        return regionIdToClaim.containsKey(id);
    }

    // Adds Claim to cache
    public void addClaim(String id, Claim claim) {
        regionIdToClaim.put(id, claim);
        centers.put(id, claim.getCenter());
    }

    // Removes Claim from cache
    public void removeClaim(String id) {
        regionIdToClaim.remove(id);
        centers.remove(id);
    }

    // Returns center of claim closest to given location
    @Nullable
    public Location getClosestTo(Location location) {
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

    public boolean isInSquare(Location location, @Nullable Location squareCenter, int squareRadius) {
        if (squareCenter != null) {
            return !(Math.abs(location.getBlockX() - squareCenter.getBlockX()) > squareRadius || Math.abs(location.getBlockZ() - squareCenter.getBlockZ()) > squareRadius);
        }
        return false;
    }

    public Claim createRegionAt(Location loc, Player owner, int level) {
        // Returning if location is too close to spawn or other claim
        if (this.isInSquare(loc, this.getClosestTo(loc), 80) || this.isInSquare(loc, ClaimsConfig.DEFAULT_WORLD.getSpawnLocation(), ClaimsConfig.MINIMUM_DISTANCE_FROM_SPAWN)) return null;
        // Points
        final UUID ownerUniqueId = owner.getUniqueId();
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        final int radius = 10 + (5 * level);
        final BlockVector3 min = BlockVector3.at(x - radius, loc.getWorld().getMinHeight(), z - radius);
        final BlockVector3 max = BlockVector3.at(x + radius, loc.getWorld().getMaxHeight(), z + radius);
        // Creating region id
        final String id = Claim.createId(loc);
        // Creating region at new points
        final ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        // Setting default flags
        this.setDefaultFlags(region, loc, owner);
        region.setFlag(ExtraFlags.CLAIM_LEVEL, level);
        // Setting region priority
        region.setPriority(ClaimsConfig.REGION_PRIORITY);
        // Adding owner
        region.getOwners().addPlayer(ownerUniqueId);
        // Registering region
        regionManager.addRegion(region);
        // Adding newly created claim to cache
        final Claim claim = new Claim(id, ownerUniqueId, region);
        this.addClaim(id, claim);
        // Making a connection between player and newly created claim
        final ClaimPlayer cp = this.getClaimPlayer(ownerUniqueId);
        cp.setClaim(claim);
        return claim;
    }

    // Existence check is already in RegionHandler
    public void removeRegionOf(UUID ownerUniqueId) {
        final ClaimPlayer cp = this.getClaimPlayer(ownerUniqueId);
        final Claim claim = cp.getClaim();
        final String id = claim.getId();
        // Removing relatives of all players added to that claim
        for (UUID member : claim.getMembers()) {
            this.getClaimPlayer(member).removeRelative(id);
        }
        // Setting owner's claim to null (because it's going to be removed in a sec)
        cp.setClaim(null);
        // Removing claim from cache
        this.removeClaim(id);
        // Removing claim from the world
        final ProtectedRegion region = claim.getWGRegion();
        regionManager.removeRegion(region.getId());
    }

    private void setDefaultFlags(ProtectedRegion region, Location loc, Player owner) {
        final String name = owner.getName();
        // Static flags (not changeable)
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        region.setFlag(Flags.WITHER_DAMAGE, StateFlag.State.DENY);
        region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.DENY);
        region.setFlag(ExtraFlags.GREETING_ACTIONBAR, ClaimsLang.DEFAULT_GREETING.replace("{player}", name));
        region.setFlag(ExtraFlags.FAREWELL_ACTIONBAR, ClaimsLang.DEFAULT_FAREWELL.replace("{player}", name));
        // Dynamic flags (changeable)
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        region.setFlag(Flags.USE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.SNOW_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.ICE_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        // Setting center location (not modifiable)
        region.setFlag(ExtraFlags.CLAIM_CENTER, BukkitAdapter.adapt(loc));
        // Setting default home location (modifiable)
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc.clone().add(0, 0.5, 0)));
    }

    public boolean upgrade(Claim claim) {
        // Ignore for levels higher than 6
        if (claim.getLevel() >= 6) return false;
        final int newLevel = claim.getLevel() + 1;
        final ProtectedRegion wgRegion = claim.getWGRegion();
        final String id = claim.getId();
        final Location center = claim.getCenter();
        // Calculating new region size
        int radius = 15 + (5 * newLevel);
        final BlockVector3 min = BlockVector3.at(center.getBlockX() - radius, center.getWorld().getMinHeight(), center.getBlockZ() - radius);
        final BlockVector3 max = BlockVector3.at(center.getBlockX() + radius, center.getWorld().getMaxHeight(), center.getBlockZ() + radius);
        // Creating cuboid at new points
        final ProtectedRegion newRegion = new ProtectedCuboidRegion(id, min, max);
        // Updating region flag
        wgRegion.setFlag(ExtraFlags.CLAIM_LEVEL, newLevel);
        // Redefining region
        newRegion.copyFrom(wgRegion);
        regionManager.addRegion(newRegion);
        // Updating Claim with new WorldGuard region
        claim.update(newRegion);
        // Updating block type ('& 0xF' thingy is doing some magic to get block's position in chunk)
        final Material type = ClaimLevel.getClaimLevel(newLevel).getClaimBlockMaterial();
        PaperLib.getChunkAtAsync(center).thenAccept(chunk -> chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type));
        return true;
    }

    @Override
    public boolean hasClaim(UUID uuid) {
        return this.getClaimPlayer(uuid).getClaim() != null;
    }

    @Override
    public @Nullable Claim getClaim(UUID uuid) {
        return this.getClaimPlayer(uuid).getClaim();
    }

    @Override
    public @Nullable Claim getClaim(String id) {
        return regionIdToClaim.get(id);
    }

    @Override
    public @NotNull ClaimPlayer getClaimPlayer(UUID uuid) {
        if (!uuidToClaimPlayer.containsKey(uuid)) {
            uuidToClaimPlayer.put(uuid, new ClaimPlayer(uuid));
        }
        return uuidToClaimPlayer.get(uuid);
    }

    @Override
    public @NotNull List<String> getClaimIds() {
        return new ArrayList<>(regionIdToClaim.keySet());
    }
}

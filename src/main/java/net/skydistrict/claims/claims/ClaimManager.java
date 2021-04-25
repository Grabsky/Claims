package net.skydistrict.claims.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.flags.ClaimFlags;
import net.skydistrict.claims.logger.FileLogger;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimManager {
    private final Claims instance;
    private final RegionManager regionManager;
    private final Map<String, Claim> regionIdToClaim = new HashMap<>();
    private final Map<UUID, ClaimPlayer> uuidToClaimPlayer = new HashMap<>();
    private final Map<String, Location> centers = new HashMap<>();

    public ClaimManager(Claims instance) {
        this.instance = instance;
        this.regionManager = instance.getRegionManager();
        this.cacheClaims();
    }

    // Should be ran only during the server startup
    private void cacheClaims() {
        for (Map.Entry<String, ProtectedRegion> en : regionManager.getRegions().entrySet()) {
            ProtectedRegion region = en.getValue();
            if (!region.getId().startsWith(Config.REGION_PREFIX) || !region.hasMembersOrOwners() || region.getOwners().size() != 1) continue;
            UUID owner = region.getOwners().getUniqueIds().iterator().next();
            ClaimPlayer cp = this.getClaimPlayer(owner);
            Claim claim = new Claim(region.getId(), owner, region);
            cp.setClaim(claim);
            // Creating members' references
            for (UUID memberUuid : region.getMembers().getUniqueIds()) {
                ClaimPlayer cm = this.getClaimPlayer(memberUuid);
                cm.addRelative(claim.getId());
            }
            // Adding claim to the cache
            String id = region.getId();
            this.addClaim(id, claim);
            System.out.println("Loaded claim owned by " + owner);
        }
    }

    /** Returns true if Claim is in cache */
    public boolean containsClaim(String id) {
        return this.regionIdToClaim.containsKey(id);
    }

    /** Returns Claim from cache */
    public Claim getClaim(String id) {
        return this.regionIdToClaim.get(id);
    }

    /** Adds Claim to cache */
    public void addClaim(String id, Claim claim) {
        this.regionIdToClaim.put(id, claim);
        this.centers.put(id, claim.getCenter());
    }

    /** Removes Claim from cache */
    public void removeClaim(String id) {
        this.regionIdToClaim.remove(id);
        this.centers.remove(id);
    }

    /** Returns ClaimPlayer from his UUID (creates object if doesn't exist) */
    public ClaimPlayer getClaimPlayer(UUID uuid) {
        if (!this.uuidToClaimPlayer.containsKey(uuid)) this.uuidToClaimPlayer.put(uuid, new ClaimPlayer(uuid));
        return this.uuidToClaimPlayer.get(uuid);
    }

    /** Returns center of claim closest to given location */
    @Nullable
    public Location getClosestTo(Location location) {
        Location closestLocation = null;
        double dist = Double.MAX_VALUE;
        for (Location loc : this.centers.values()) {
            double d = loc.distance(location);
            if (d < dist) {
                closestLocation = loc;
                dist = d;
            }
        }
        return closestLocation;
    }

    public boolean canPlaceAt(Location location) {
        Location center = this.getClosestTo(location);
        if (center == null) return true;
        return (Math.abs(location.getBlockX() - center.getBlockX()) > 70 || Math.abs(location.getBlockZ() - center.getBlockZ()) > 70);
    }

    public boolean createRegionAt(Location loc, Player owner, int level) {
        // Checking if there is no region at this selection
        if (!this.canPlaceAt(loc) || loc.distance(Config.DEFAULT_WORLD.getSpawnLocation()) < Config.MINIMUM_DISTANCE_FROM_SPAWN) return false;
        // Points
        UUID ownerUniqueId = owner.getUniqueId();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int radius = 15 + (5 * level);
        BlockVector3 min = BlockVector3.at(x - radius, 0, z - radius);
        BlockVector3 max = BlockVector3.at(x + radius, 255, z + radius);
        // Creating region id
        String id = ClaimH.createId(loc);
        // Creating region at new points
        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        // Setting default flags
        region.setFlag(ClaimFlags.CLAIM_LEVEL, level);
        this.setDefaultFlags(region, loc, owner);
        // Adding owner
        region.getOwners().addPlayer(ownerUniqueId);
        // Registering region
        regionManager.addRegion(region);
        // Adding newly created claim to cache
        Claim claim = new Claim(id, ownerUniqueId, region);
        this.addClaim(id, claim);
        // Making a connection between player and newly created claim
        ClaimPlayer cp = this.getClaimPlayer(ownerUniqueId);
        cp.setClaim(claim);
        FileLogger.log(new StringBuilder()
                .append("CLAIM_CREATED | ")
                .append(id).append(" (").append(claim.getLevel()).append(") | ")
                .append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(" | ")
                .append(owner.getName()).append(" (").append(owner.getUniqueId()).append(")").toString());
        return true;
    }

    // Existence check is already in RegionHandler
    public void removeRegionOf(Player player, UUID ownerUniqueId) {
        ClaimPlayer cp = this.getClaimPlayer(ownerUniqueId);
        Claim claim = cp.getClaim();
        String id = claim.getId();
        // Removing relatives of all players added to that claim
        for (UUID member : claim.getMembers()) {
            this.getClaimPlayer(member).removeRelative(id);
        }
        // Setting owner's claim to null (because it's going to be removed in a sec)
        cp.setClaim(null);
        // Removing claim from cache
        this.removeClaim(id);
        // Removing claim from the world
        ProtectedRegion region = claim.getWGRegion();
        regionManager.removeRegion(region.getId());
        Location loc = claim.getCenter();
        FileLogger.log(new StringBuilder()
                .append("CLAIM_DESTROYED | ")
                .append(id).append(" (").append(claim.getLevel()).append(") | ")
                .append(loc.getBlockX()).append(", ").append(loc.getBlockY()).append(", ").append(loc.getBlockZ()).append(" | ")
                .append(player.getName()).append(" (").append(player.getUniqueId()).append(") | ")
                .append(UUIDCache.get(ownerUniqueId)).append(" (").append(ownerUniqueId).append(")")
                .toString());
    }

    public boolean upgrade(Claim claim) {
        if (claim.getLevel() >= 4) return false;
        int newLevel = claim.getLevel() + 1;
        ProtectedRegion wgRegion = claim.getWGRegion();
        String id = claim.getId();
        Location center = claim.getCenter();
        // Calculating new region size
        int radius = 15 + (5 * newLevel);
        BlockVector3 min = BlockVector3.at(center.getBlockX() - radius, 0, center.getBlockZ() - radius);
        BlockVector3 max = BlockVector3.at(center.getBlockX() + radius, 255, center.getBlockZ() + radius);
        // Creating cuboid at new points
        ProtectedRegion newRegion = new ProtectedCuboidRegion(id, min, max);
        // Updating region flag
        wgRegion.setFlag(ClaimFlags.CLAIM_LEVEL, newLevel);
        // Redefining region
        newRegion.copyFrom(wgRegion);
        regionManager.addRegion(newRegion);
        // Updating Claim with new WorldGuard region
        claim.update(newRegion);
        // Updating block type ('& 0xF' thingy is doing some magic to get block's position in chunk)
        Material type = ClaimH.getClaimLevel(newLevel).getBlockMaterial();
        PaperLib.getChunkAtAsync(center).thenAccept(chunk -> chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type));
        return true;
    }

    private void setDefaultFlags(ProtectedRegion region, Location loc, Player owner) {
        String name = owner.getName();
        // Static flags (not changeable)
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.WITHER_DAMAGE, StateFlag.State.DENY);
        region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.DENY);
        region.setFlag(ClaimFlags.GREETING_ACTIONBAR, MessageFormat.format(Lang.DEFAULT_GREETING, name));
        region.setFlag(ClaimFlags.FAREWELL_ACTIONBAR, MessageFormat.format(Lang.DEFAULT_FAREWELL, name));
        // Dynamic flags (changeable)
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        region.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.SNOW_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.ICE_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        // Setting center location (not modifiable)
        region.setFlag(ClaimFlags.CLAIM_CENTER, BukkitAdapter.adapt(loc));
        // Setting default home location (modifiable)
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc.clone().add(0, 0.5, 0)));
    }

}

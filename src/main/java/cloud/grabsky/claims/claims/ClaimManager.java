package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.Claims.CustomFlag;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.configuration.adapter.ClaimTypeAdapterFactory;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.configuration.paper.adapter.ComponentAdapter;
import cloud.grabsky.configuration.paper.adapter.EnchantmentAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.EnchantmentEntryAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.EntityTypeAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.ItemFlagAdapter;
import cloud.grabsky.configuration.paper.adapter.ItemStackAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.MaterialAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import cloud.grabsky.configuration.paper.adapter.PersistentDataEntryAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.PersistentDataTypeAdapterFactory;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.squareup.moshi.Moshi;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import okio.BufferedSource;
import okio.Okio;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;

public final class ClaimManager {

    private final Claims claims;

    private final RegionManager regionManager;
    private final Map<String, Claim> claimsCache = new HashMap<>();
    private final Map<UUID, ClaimPlayer> claimPlayerCache = new HashMap<>();

    @Getter(AccessLevel.PUBLIC)
    private final Map<String, Claim.Type> claimTypes = new HashMap<>();

    public ClaimManager(final Claims claims, final RegionManager regionManager) {
        this.claims = claims;
        this.regionManager = regionManager;
        // ...
        this.cacheClaimTypes();
        this.cacheClaims();
    }

    @Internal
    private void cacheClaimTypes() throws IllegalStateException {
        final File typesDirectory = new File(claims.getDataFolder(), "types");
        // Creating /plugins/Claims/types directory if does not exist.
        if (typesDirectory.exists() == false)
            typesDirectory.mkdirs();
        // Throwing exception if /plugins/Claims/types is not a directory.
        if (typesDirectory.isDirectory() == false)
            throw new IllegalStateException(typesDirectory.getPath() + " is not a directory. Read documentation at [DOCS_LINK] to learn how to define claim types.");
        // ...
        final File[] files = typesDirectory.listFiles();
        // Guiding to read docs if no claim types are defined.
        if (files == null)
            throw new IllegalStateException(typesDirectory.getPath() + " is not a directory. Read documentation at [DOCS_LINK] to learn how to define claim types.");
        // Filtering and sorting
        final List<File> sortedFiles = stream(files)
                .filter(file -> file.getName().endsWith(".json"))
                .sorted(comparing(File::getName).reversed())
                .toList();
        // ...
        if (sortedFiles.isEmpty() == true) {
            claims.getLogger().warning("No claim types has been defined inside /plugins/Claims/types/ directory. Read documentation at [DOCS_LINK] to learn how.");
            return;
        }
        // ...
        final Moshi moshi = new Moshi.Builder()
                // Everything needed for ItemStack deserialization...
                .add(Component.class, ComponentAdapter.INSTANCE)
                .add(ItemFlag.class, ItemFlagAdapter.INSTANCE)
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(EnchantmentAdapterFactory.INSTANCE)
                .add(EnchantmentEntryAdapterFactory.INSTANCE)
                .add(EntityTypeAdapterFactory.INSTANCE)
                .add(ItemStackAdapterFactory.INSTANCE)
                .add(MaterialAdapterFactory.INSTANCE)
                .add(PersistentDataEntryAdapterFactory.INSTANCE)
                .add(PersistentDataTypeAdapterFactory.INSTANCE)
                // ClaimType deserialization...
                .add(new ClaimTypeAdapterFactory(this))
                .build();
        // ...
        Claim.Type previous = null;
        // ...
        int totalClaimTypes = 0;
        int loadedClaimTypes = 0;
        for (final File file : sortedFiles) {
            try {
                final BufferedSource buffer = Okio.buffer(Okio.source(file));
                // ...
                totalClaimTypes++;
                // Reading
                final Claim.Type type = moshi.adapter(Claim.Type.class).lenient().fromJson(buffer);
                // ...
                if (type == null) {
                    claims.getLogger().warning("Claim type cannot be loaded. (FILE = " + file.getPath() + ")");
                    continue;
                }
                // ...
                type.setNextType(previous);
                // ...
                claimTypes.put(type.getId(), type);
                // ...
                previous = type;
                // ...
                loadedClaimTypes++;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        // ...
        claims.getLogger().info("Loaded " + loadedClaimTypes + " out of " + totalClaimTypes + " claim types total.");
    }

    @Internal
    private void cacheClaims() throws IllegalStateException {
        int totalClaims = 0;
        int loadedClaims = 0;
        for (final var entry : regionManager.getRegions().entrySet()) {
            final String id = entry.getKey();
            final ProtectedRegion region = entry.getValue();
            // Skipping regions not starting with configured prefix, or regions which owner count =/= 1.
            if (region.getId().startsWith(PluginConfig.REGION_PREFIX) == false)
                return;
            // ...
            totalClaims++;
            // ...
            final String claimTypeId = region.getFlag(CustomFlag.CLAIM_TYPE);
            final @Nullable Claim.Type claimType = claimTypes.get(claimTypeId);
            // Skipping claims with non-existent or invalid type.
            if (claimTypeId == null || claimTypes.containsKey(claimTypeId) == false) {
                claims.getLogger().warning("Claim cannot be loaded because it's TYPE is not defined. (CLAIM_ID = " + id + ", CLAIM_TYPE_ID = " + claimTypeId + ")");
                continue;
            }
            final Claim claim = new Claim(id, this, region, claimType);
            // ...
            region.getOwners().getUniqueIds().forEach(uuid -> {
                final ClaimPlayer claimOwner = this.getClaimPlayer(uuid);
                // ...
                claimOwner.addClaim(claim);
            });
            // Adding claim to the cache
            claimsCache.put(id, claim);
            // ...
            loadedClaims++;
        }
        // ...
        claims.getLogger().info("Loaded " + loadedClaims + " out of " + totalClaims + " claims total.");
        // ...
        if (loadedClaims < totalClaims) {
            claims.getLogger().warning("Unloaded claims ARE STILL PROTECTED but are excluded from plugin cache and are inaccessible by players. You should take a closer look at all of them individually to see what's wrong.");
        }
    }

    // Returns true if Claim exists and is currently cached.
    public boolean containsClaim(final @NotNull String id) {
        // Removing "stale" regions if they don't exist in world anymore.
        if (regionManager.hasRegion(id) == false) {
            claimsCache.remove(id);
        }
        // ...
        return claimsCache.containsKey(id);
    }

    // Returns true if Claim exists and is currently cached.
    public boolean containsClaim(final @NotNull Claim claim) {
        return containsClaim(claim.getId());
    }


    // Returns center of claim closest to given location
    public @Nullable Location getClosestTo(final @NotNull Location location) {
        return claimsCache.values().stream()
                .map(Claim::getCenter)
                .min(comparingDouble(location::distance))
                .orElse(null);
    }

    public boolean isInSquare(final @NotNull Location location, final @Nullable Location squareCenter, final int squareRadius) {
        if (squareCenter != null) {
            return !(Math.abs(location.getBlockX() - squareCenter.getBlockX()) > squareRadius || Math.abs(location.getBlockZ() - squareCenter.getBlockZ()) > squareRadius);
        }
        return false;
    }

    public Claim createClaim(final Location loc, final Player owner, final String typeName) {
        // Returning if location is too close to spawn or other claim
        if (this.isInSquare(loc, this.getClosestTo(loc), 80) == true || this.isInSquare(loc, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN) == true)
            return null;
        // Points
        final UUID ownerUniqueId = owner.getUniqueId();
        // ...
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        // ...
        final Claim.Type type = claimTypes.get(typeName);
        // ...
        final int radius = type.getRadius();
        // ...
        final BlockVector3 min = BlockVector3.at(x - radius, loc.getWorld().getMinHeight(), z - radius);
        final BlockVector3 max = BlockVector3.at(x + radius, loc.getWorld().getMaxHeight(), z + radius);
        // Creating region id
        final String id = Claim.createId(loc);
        // Creating region at new points
        final ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        // Setting default flags
        this.setDefaultFlags(region, loc, owner);
        region.setFlag(CustomFlag.CLAIM_TYPE, type.getId());
        // Setting region priority
        region.setPriority(PluginConfig.REGION_PRIORITY);
        // Adding owner
        region.getOwners().addPlayer(ownerUniqueId);
        // Registering region
        regionManager.addRegion(region);
        // Adding newly created claim to cache
        final ClaimPlayer claimOwner = this.getClaimPlayer(ownerUniqueId);
        // ...
        final Claim claim = new Claim(id, this, region, type);
        claimsCache.put(id, claim);
        // Making a connection between player and newly created claim
        claimOwner.addClaim(claim);
        return claim;
    }

    // Existence check is already in RegionHandler
    public void deleteClaim(final Claim claim) {
        final String id = claim.getRegion().getId();
        // Setting owner's claim to null (because it's going to be removed in a sec)
        claim.getOwners().forEach(owner -> owner.removeClaim(claim));
        // Removing claim from cache
        claimsCache.remove(id);
        // Removing claim from the world
        final ProtectedRegion region = claim.getRegion();
        regionManager.removeRegion(region.getId());
    }

    public @Nullable Claim getClaimAt(final @NotNull Location location) {
        final List<String> ids = regionManager.getApplicableRegionsIDs(adapt(location).toVector().toBlockPoint())
                .stream().filter(id -> id.startsWith(PluginConfig.REGION_PREFIX) == true).toList();
        // ...
        if (ids.isEmpty() == true || ids.size() > 1)
            return null;
        // ...
        final @Nullable Claim claim = this.getClaim(ids.iterator().next());
        // ...
        if (claim != null && this.containsClaim(claim) == true)
            return claim;
        // ...
        return null;
    }

    private void setDefaultFlags(final @NotNull ProtectedRegion region, final @NotNull Location center, final Player owner) {
        final String ownerName = owner.getName();
        final com.sk89q.worldedit.util.Location regionCenter = adapt(center);
        // Static flags (not changeable)
        region.setFlag(Flags.PVP,                  StateFlag.State.DENY);
        region.setFlag(Flags.WITHER_DAMAGE,        StateFlag.State.DENY);
        region.setFlag(Flags.GHAST_FIREBALL,       StateFlag.State.DENY);
        region.setFlag(CustomFlag.ENTER_ACTIONBAR, PluginLocale.FLAGS_CLAIM_ENTER.replace("<player>", ownerName));
        region.setFlag(CustomFlag.LEAVE_ACTIONBAR, PluginLocale.FLAGS_CLAIM_LEAVE.replace("<player>", ownerName));
        // Flag policy
        region.setFlag(Flags.USE.getRegionGroupFlag(),          RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        // Dynamic flags (changeable)
        region.setFlag(Flags.USE,                 PluginFlags.USE.getDefaultValue());
        region.setFlag(Flags.CHEST_ACCESS,        PluginFlags.CHEST_ACCESS.getDefaultValue());
        region.setFlag(Flags.TNT,                 PluginFlags.TNT.getDefaultValue());
        region.setFlag(Flags.CREEPER_EXPLOSION,   PluginFlags.CREEPER_EXPLOSION.getDefaultValue());
        region.setFlag(Flags.SNOW_MELT,           PluginFlags.SNOW_MELT.getDefaultValue());
        region.setFlag(Flags.ICE_MELT,            PluginFlags.ICE_MELT.getDefaultValue());
        region.setFlag(Flags.FIRE_SPREAD,         PluginFlags.FIRE_SPREAD.getDefaultValue());
        region.setFlag(Flags.MOB_SPAWNING,        PluginFlags.MOB_SPAWNING.getDefaultValue());
        region.setFlag(CustomFlag.CLIENT_TIME,    PluginFlags.CLIENT_TIME.getDefaultValue());
        region.setFlag(CustomFlag.CLIENT_WEATHER, PluginFlags.CLIENT_WEATHER.getDefaultValue());
        // Setting center location (not modifiable)
        region.setFlag(CustomFlag.CLAIM_CENTER, regionCenter);
        // Setting default home location (modifiable)
        region.setFlag(Flags.TELE_LOC, regionCenter.setY(regionCenter.getY() + 0.5F));
    }

    public boolean upgradeClaim(final @NotNull Claim claim) throws ClaimProcessException {
        if (this.containsClaim(claim) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        // Ignore for levels higher than 6
        if (claim.getType().isUpgradeable() == false)
            return false;
        // ...
        final Claim.Type newType = claim.getType().getNextType();
        // ...
        final ProtectedRegion region = claim.getRegion();
        // ...
        final Location center = claim.getCenter();
        // Calculating new region size
        final int radius = newType.getRadius();
        final BlockVector3 min = BlockVector3.at(center.getBlockX() - radius, center.getWorld().getMinHeight(), center.getBlockZ() - radius);
        final BlockVector3 max = BlockVector3.at(center.getBlockX() + radius, center.getWorld().getMaxHeight(), center.getBlockZ() + radius);
        // Creating cuboid at new points
        final ProtectedRegion newRegion = new ProtectedCuboidRegion(claim.getId(), min, max);
        // Updating region flag
        region.setFlag(CustomFlag.CLAIM_TYPE, newType.getId());
        // Redefining region
        newRegion.copyFrom(region);
        regionManager.addRegion(newRegion);
        // Updating Claim with new WorldGuard region
        claim.setRegion(newRegion);
        claim.setType(newType);
        // Updating block type ('& 0xF' thingy is doing some magic to get block's position in chunk)
        final Material type = newType.getBlock().getType();
        center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type));
        return true;
    }

    public @Nullable Claim getClaim(final @NotNull String id) {
        return claimsCache.get(id);
    }

    public @Nullable Claim getClaim(final @NotNull ProtectedRegion region) {
        return claimsCache.get(region.getId());
    }

    public @NotNull ClaimPlayer getClaimPlayer(final @NotNull UUID uuid) {
        return claimPlayerCache.computeIfAbsent(uuid, (u) -> new ClaimPlayer(this, u));
    }

    public @NotNull ClaimPlayer getClaimPlayer(final @NotNull Player player) {
        return claimPlayerCache.computeIfAbsent(player.getUniqueId(), (uuid) -> new ClaimPlayer(this, uuid));
    }

    public @NotNull Set<String> getClaimIds() {
        return Collections.unmodifiableSet(claimsCache.keySet());
    }

    public @NotNull Collection<Claim> getClaims() {
        return Collections.unmodifiableCollection(claimsCache.values());
    }

}

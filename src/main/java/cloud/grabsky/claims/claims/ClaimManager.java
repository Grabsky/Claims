/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
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
import net.kyori.adventure.text.Component;
import okio.BufferedSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;
import static java.lang.Math.abs;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.Comparator.comparing;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class ClaimManager {

    @Getter(AccessLevel.PUBLIC)
    private final Claims plugin;

    @Getter(AccessLevel.PUBLIC)
    private final RegionManager regionManager;

    private final Map<String, Claim> claimsCache = new HashMap<>();
    private final Map<UUID, ClaimPlayer> claimPlayerCache = new HashMap<>();

    @Getter(AccessLevel.PUBLIC)
    private final LinkedHashMap<String, Claim.Type> claimTypes = new LinkedHashMap<>();

    public ClaimManager(final Claims plugin, final RegionManager regionManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        // ...
        this.cacheClaimTypes();
        this.cacheClaims();
    }

    /**
     * Loads and caches defined {@link Claim.Type} objects defined in configuration files.
     */
    private void cacheClaimTypes() throws IllegalStateException {
        final File directory = new File(plugin.getDataFolder(), "types");
        // Listing files inside directory. Can be null for non-existent or non-directory files.
        final File @Nullable [] files = directory.listFiles();
        // Creating /plugins/Claims/types directory if does not exist.
        if (directory.mkdirs() == true || files == null || files.length == 0) {
            plugin.getLogger().warning("No files found inside \"" + directory + "\" directory... Read documentation at [DOCS_LINK] for more information.");
            return;
        }
        // Creating a new instance of Moshi.
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
                // Claim.Type deserialization...
                .add(new ClaimTypeAdapterFactory(this))
                .build();
        // Creating (empty) AtomicReference to keep track of previous Claim.Type instance.
        final AtomicReference<Claim.Type> previous = new AtomicReference<>();
        // Some numbers to be incremented and displayed later on...
        final AtomicInteger totalClaimTypes = new AtomicInteger(0);
        final AtomicInteger loadedClaimTypes = new AtomicInteger(0);
        // Iterating over sorted and filtered files.
        // TO-DO: Lexicographical sort is going to fail for numbers greater than 10. Alternative sorting algorithm must be used.
        Stream.of(files).sorted(comparing(File::getName).reversed()).filter(file -> file.getName().endsWith(".json") == true).forEach(file -> {
            // Incrementing total number of files as we're about to attempt loading.
            totalClaimTypes.incrementAndGet();
            // Trying...
            try (final BufferedSource buffer = buffer(source(file))) {
                // Reading new Claim.Type instance from JSON.
                final Claim.Type type = moshi.adapter(Claim.Type.class).lenient().fromJson(buffer);
                // Logging error in case result ended up being null.
                if (type == null) {
                    plugin.getLogger().severe("Could not load claim type defined inside \"" + file + "\" file.");
                    plugin.getLogger().severe("  null");
                    // Continuing to the next file...
                    return;
                }
                final @Nullable Claim.Type previousClaimType = previous.get();
                // Setting next type of
                type.setNextType(previousClaimType);
                // Adding new
                claimTypes.put(type.getId(), type);
                // Updating the reference.
                previous.set(type);
                // Incrementing number of files that were successfully loaded.
                loadedClaimTypes.incrementAndGet();
            } catch (final IOException e) {
                plugin.getLogger().severe("Could not load claim type defined inside \"" + file + "\" file.");
                plugin.getLogger().severe("  " + e.getMessage());
            }
        });
        // Printing numbers to the console.
        plugin.getLogger().info("Successfully loaded " + loadedClaimTypes + " out of " + totalClaimTypes + " claim types total.");
    }

    /**
     * Loads and caches {@link Claim} objects and related components.
     */
    // TO-DO: CLEAN THAT UP
    public void cacheClaims() throws IllegalStateException {
        this.claimsCache.clear();
        // ...
        final AtomicInteger totalClaims = new AtomicInteger(0);
        final AtomicInteger loadedClaims = new AtomicInteger(0);
        // Iterating over all regions
        regionManager.getRegions().forEach((id, region) -> {
            // Skipping regions that are not starting with configured prefix.
            if (region.getId().startsWith(PluginConfig.REGION_PREFIX) == false)
                return;
            // ...
            totalClaims.incrementAndGet();
            // ...
            final String claimTypeId = region.getFlag(CustomFlag.CLAIM_TYPE);
            final @Nullable Claim.Type claimType = claimTypes.get(claimTypeId);
            // Skipping claims with non-existent or invalid type.
            if (claimType == null || claimTypes.containsKey(claimTypeId) == false) {
                plugin.getLogger().warning("Claim cannot be loaded because it's TYPE is not defined. (CLAIM_ID = " + id + ", CLAIM_TYPE_ID = " + claimTypeId + ")");
                return;
            }
            final Claim claim = new Claim(id, this, region, claimType);
            // Adding claim to the cache.
            claimsCache.put(id, claim);
            // ...
            loadedClaims.incrementAndGet();
        });
        // ...
        plugin.getLogger().info("Successfully loaded " + loadedClaims + " out of " + totalClaims + " claims total.");
        // ...
        if (totalClaims.get() - loadedClaims.get() > 0) {
            plugin.getLogger().warning("Not loaded claims ARE STILL PROTECTED but are excluded from plugin cache and are inaccessible by players. You should take a closer look at all of them individually to see what's wrong.");
        }
    }

    /**
     * Returns {@code true} if {@link Claim} identified with provided id exists and is currently cached.
     */
    public boolean containsClaim(final @NotNull String id) {
        // Removing "stale" regions if they don't exist in world anymore.
        if (regionManager.hasRegion(id) == false)
            claimsCache.remove(id);
        // ...
        return claimsCache.containsKey(id);
    }

    /**
     * Returns {@code true} if {@link Claim} exists and is currently cached.
     */
    public boolean containsClaim(final @NotNull Claim claim) {
        return containsClaim(claim.getId());
    }

    /**
     * Tries to create {@link Claim} (and {@link ProtectedRegion} it relies on) at the specified location.
     */
    public @Nullable Claim createClaim(final @NotNull Location location, final @NotNull Player owner, final @NotNull Claim.Type type) {
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        // Getting the largest claim radius, assuming it is always the last one defined. (LAST CLAIM TYPE)
        final int maxRadius = claimTypes.values().iterator().next().getRadius();
        // Calculating ultimate boundaries.
        final BlockVector3 tMin = BlockVector3.at(x - maxRadius, location.getWorld().getMinHeight(), z - maxRadius);
        final BlockVector3 tMax = BlockVector3.at(x + maxRadius, location.getWorld().getMaxHeight(), z + maxRadius);
        // Checking boundaries are not colliding with any other regions, including other, not fully upgraded claims.
        final boolean isColliding = regionManager.getApplicableRegions(new ProtectedCuboidRegion("_", true, tMin.subtract(maxRadius, 0, maxRadius), tMax.add(maxRadius, 0, maxRadius))).getRegions().stream()
                .anyMatch(region -> {
                    // Ignoring regions with lower priority.
                    if (region.getPriority() < PluginConfig.REGION_PRIORITY)
                        return false;
                    // Regular regions.
                    if (region.getId().startsWith(PluginConfig.REGION_PREFIX) == false)
                        return isColliding(tMin, tMax, region.getMinimumPoint(), region.getMaximumPoint());
                    // Claims.
                    final BlockVector3 center = region.getMinimumPoint().add(region.getMaximumPoint()).divide(2);
                    return isColliding(tMin, tMax, center.subtract(maxRadius, 0, maxRadius), center.add(maxRadius, 0, maxRadius));
                });
        // Returning null if region is colliding with a region nearby.
        if (isColliding == true)
            return null;
        // Getting radius of specified claim type.
        final int radius = type.getRadius();
        // Calculating initial boundaries.
        final BlockVector3 min = BlockVector3.at(x - radius, location.getWorld().getMinHeight(), z - radius);
        final BlockVector3 max = BlockVector3.at(x + radius, location.getWorld().getMaxHeight(), z + radius);
        // Creating region identifier from its center location.
        final String id = Claim.createId(location);
        // Creating a new WorldGuard region within the calculated boundaries.
        final ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        // Setting default flags
        setDefaultFlags(region, location, owner);
        region.setFlag(CustomFlag.CLAIM_TYPE, type.getId());
        // Setting region priority.
        region.setPriority(PluginConfig.REGION_PRIORITY);
        // Setting region owner.
        region.getOwners().addPlayer(owner.getUniqueId());
        // Adding WorldGuard region to the RegionManager.
        regionManager.addRegion(region);
        // Creating Claim object and caching it.
        final Claim claim = new Claim(id, this, region, type);
        claimsCache.put(id, claim);
        // Returning Claim object.
        return claim;
    }

    /**
     * Deletes specified {@link Claim} and associated {@link ProtectedRegion}.
     */
    public void deleteClaim(final Claim claim) {
        final String id = claim.getRegion().getId();
        // Removing claim from cache
        claimsCache.remove(id);
        // Removing claim from the world
        regionManager.removeRegion(id);
    }

    /**
     * Returns {@link Claim} at specified {@link Location} or {@code null} if none or multiple claims are found.
     */
    public @Nullable Claim getClaimAt(final @NotNull Location location) {
        final List<String> ids = regionManager.getApplicableRegionsIDs(adapt(location).toVector().toBlockPoint())
                .stream().filter(id -> id.startsWith(PluginConfig.REGION_PREFIX) == true).toList();
        // ...
        if (ids.size() != 1)
            return null;
        // ...
        final @Nullable Claim claim = this.getClaim(ids.iterator().next());
        // ...
        if (claim != null && this.containsClaim(claim) == true)
            return claim;
        // ...
        return null;
    }

    /**
     * Upgrades provided {@link Claim}.
     * Returns {@code true} if successful or {@code false} if {@link Claim.Type} is not upgrade-able.
     */
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
        // Updating block type '& 0xF' thingy is doing some magic to get block's position in chunk)
        final Material type = newType.getBlock().getType();
        center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type));
        return true;
    }

    @Experimental
    public static boolean isWithinSquare(final @NotNull Location location, final @Nullable Location squareCenter, final int squareRadius) {
        return squareCenter != null && (abs(location.getBlockX() - squareCenter.getBlockX()) > squareRadius || abs(location.getBlockZ() - squareCenter.getBlockZ()) > squareRadius) == false;
    }

    @Experimental
    private static boolean isColliding(final BlockVector3 min, final BlockVector3 max, final BlockVector3 otherMin, final BlockVector3 otherMax) {
        return min.getBlockX() <= otherMax.getBlockX()
                && min.getBlockY() <= otherMax.getBlockY()
                && min.getBlockZ() <= otherMax.getBlockZ()
                && max.getBlockX() >= otherMin.getBlockX()
                && max.getBlockY() >= otherMin.getBlockY()
                && max.getBlockZ() >= otherMin.getBlockZ();
    }

    private static void setDefaultFlags(final @NotNull ProtectedRegion region, final @NotNull Location center, final @NotNull Player owner) {
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
        region.setFlag(CustomFlag.CLAIM_CREATED, valueOf(currentTimeMillis()));
        region.setFlag(CustomFlag.CLAIM_CENTER, regionCenter);
        // Setting default home location (modifiable)
        region.setFlag(Flags.TELE_LOC, regionCenter.setY(regionCenter.getY() + 0.5F));
        // Overriding global 'blocked-cmds' flag (if enabled)
        if (PluginConfig.CLAIMS_SETTINGS_OVERRIDE_BLOCKED_CMDS_FLAG) {
            region.setFlag(Flags.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.MEMBERS);
            region.setFlag(Flags.BLOCKED_CMDS, Set.of(" "));
        }
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

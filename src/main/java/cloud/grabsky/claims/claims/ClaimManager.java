package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.api.ClaimsAPI;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.configuration.adapter.ClaimTypeAdapterFactory;
import cloud.grabsky.configuration.paper.adapter.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.squareup.moshi.Moshi;
import io.papermc.lib.PaperLib;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import okio.BufferedSource;
import okio.Okio;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

public final class ClaimManager implements ClaimsAPI {

    private final Claims claims;
    private final Moshi moshi;

    private final RegionManager regionManager;
    private final Map<String, Claim> claimsCache = new HashMap<>();
    private final Map<UUID, ClaimPlayer> claimPlayerCache = new HashMap<>();

    @Getter(AccessLevel.PUBLIC)
    private final TreeMap<String, Claim.Type> claimTypes = new TreeMap<>();

    public ClaimManager(final Claims claims) {
        this.claims = claims;
        this.regionManager = claims.getRegionManager();
        // ...
        this.moshi = new Moshi.Builder()
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
                .add(SoundAdapterFactory.INSTANCE)
                .add(new ClaimTypeAdapterFactory(this))
                .build();
    }

    public void loadClaimLevels() {
        final File typesDirectory = new File(claims.getDataFolder(), "types");
        // ...
        if (typesDirectory.exists() == false || typesDirectory.isDirectory() == false)
            throw new IllegalStateException(typesDirectory.getPath() + " does not exist or is not a directory.");
        // ...
        final File[] files = typesDirectory.listFiles();
        // ...
        if (files == null || files.length == 0)
            throw new IllegalStateException("No claims defined.");
        // ,,,
        final List<File> sortedFiles = stream(files).sorted(comparing(File::getName).reversed()).toList();
        // ...
        Claim.Type previous = null;
        // ...
        for (final File file : sortedFiles) {
            try {
                final BufferedSource buffer = Okio.buffer(Okio.source(file));
                // ...
                final Claim.Type type = moshi.adapter(Claim.Type.class).lenient().fromJson(buffer);
                // ...
                if (type != null) {
                    type.setNextType(previous);
                    // ...
                    claimTypes.put(type.getUniqueId(), type);
                    // ...
                    previous = type;
                }
            } catch (final IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    // Should be run only during the server startup
    public void cacheClaims() {
        int loadedClaims = 0;
        for (Map.Entry<String, ProtectedRegion> en : regionManager.getRegions().entrySet()) {
            final ProtectedRegion region = en.getValue();
            if (!region.getId().startsWith(PluginConfig.REGION_PREFIX) || !region.hasMembersOrOwners() || region.getOwners().size() != 1) continue;
            final UUID owner = region.getOwners().getUniqueIds().iterator().next();
            final ClaimPlayer claimOwner = this.getClaimPlayer(owner);
            final Claim.Type typeId = claimTypes.get(region.getFlag(Claims.CustomFlag.CLAIM_TYPE));
            final Claim claim = new Claim(this, claimOwner, region, typeId);
            claimOwner.setClaim(claim);
            // Creating members' references
            for (final UUID memberUuid : region.getMembers().getUniqueIds()) {
                ClaimPlayer cm = this.getClaimPlayer(memberUuid);
                // cm.addRelativeClaim(claim.getId());
            }
            // Adding claim to the cache
            final String id = region.getId();
            this.addClaim(id, claim);
            loadedClaims++;
        }
        claims.getLogger().info("Loaded " + loadedClaims + " claims.");
    }

    // Returns true if Claim is in cache
    public boolean containsClaim(String id) {
        return claimsCache.containsKey(id);
    }

    // Adds Claim to cache
    public void addClaim(String id, Claim claim) {
        claimsCache.put(id, claim);
    }

    // Removes Claim from cache
    public void removeClaim(String id) {
        claimsCache.remove(id);
    }

    // Returns center of claim closest to given location
    public @Nullable Location getClosestTo(final @NotNull Location location) {
        return claimsCache.values().stream()
                .map(Claim::getCenter)
                .min(Comparator.comparingDouble(location::distance))
                .orElse(null);
    }

    public boolean isInSquare(final @NotNull Location location, final @Nullable Location squareCenter, final int squareRadius) {
        if (squareCenter != null) {
            return !(Math.abs(location.getBlockX() - squareCenter.getBlockX()) > squareRadius || Math.abs(location.getBlockZ() - squareCenter.getBlockZ()) > squareRadius);
        }
        return false;
    }

    public Claim createRegionAt(final Location loc, final Player owner, final String typeName) {
        // Returning if location is too close to spawn or other claim
        if (this.isInSquare(loc, this.getClosestTo(loc), 80) || this.isInSquare(loc, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN))
            return null;
        // Points
        final UUID ownerUniqueId = owner.getUniqueId();
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
        region.setFlag(Claims.CustomFlag.CLAIM_TYPE, type.getUniqueId());
        // Setting region priority
        region.setPriority(PluginConfig.REGION_PRIORITY);
        // Adding owner
        region.getOwners().addPlayer(ownerUniqueId);
        // Registering region
        regionManager.addRegion(region);
        // Adding newly created claim to cache
        final ClaimPlayer claimOwner = this.getClaimPlayer(ownerUniqueId);
        // ...
        final Claim claim = new Claim(this, claimOwner, region, type);
        this.addClaim(id, claim);
        // Making a connection between player and newly created claim
        claimOwner.setClaim(claim);
        return claim;
    }

    // Existence check is already in RegionHandler
    public void removeRegionOf(final UUID ownerUniqueId) {
        final ClaimPlayer cp = this.getClaimPlayer(ownerUniqueId);
        final Claim claim = cp.getClaim();
        final String id = claim.getRegion().getId();
        // Setting owner's claim to null (because it's going to be removed in a sec)
        cp.setClaim(null);
        // Removing claim from cache
        this.removeClaim(id);
        // Removing claim from the world
        final ProtectedRegion region = claim.getRegion();
        regionManager.removeRegion(region.getId());
    }

    private void setDefaultFlags(ProtectedRegion region, Location loc, Player owner) {
        final String name = owner.getName();
        // Static flags (not changeable)
        region.setFlag(Flags.PVP,                         StateFlag.State.DENY);
        region.setFlag(Flags.WITHER_DAMAGE,               StateFlag.State.DENY);
        region.setFlag(Flags.GHAST_FIREBALL,              StateFlag.State.DENY);
        region.setFlag(Claims.CustomFlag.ENTER_ACTIONBAR, PluginLocale.DEFAULT_GREETING.replace("{player}", name));
        region.setFlag(Claims.CustomFlag.LEAVE_ACTIONBAR, PluginLocale.DEFAULT_FAREWELL.replace("{player}", name));
        // Flag policy
        region.setFlag(Flags.USE.getRegionGroupFlag(),   RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        // Dynamic flags (changeable)
        region.setFlag(Flags.USE,               PluginFlags.USE.getDefaultValue());
        region.setFlag(Flags.ENTRY,             PluginFlags.ENTRY.getDefaultValue());
        region.setFlag(Flags.TNT,               PluginFlags.TNT.getDefaultValue());
        region.setFlag(Flags.CREEPER_EXPLOSION, PluginFlags.CREEPER_EXPLOSION.getDefaultValue());
        region.setFlag(Flags.SNOW_MELT,         PluginFlags.SNOW_MELT.getDefaultValue());
        region.setFlag(Flags.ICE_MELT,          PluginFlags.ICE_MELT.getDefaultValue());
        region.setFlag(Flags.FIRE_SPREAD,       PluginFlags.FIRE_SPREAD.getDefaultValue());
        region.setFlag(Flags.MOB_SPAWNING,      PluginFlags.MOB_SPAWNING.getDefaultValue());
        // Setting center location (not modifiable)
        region.setFlag(Claims.CustomFlag.CLAIM_CENTER, BukkitAdapter.adapt(loc));
        // Setting default home location (modifiable)
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc.clone().add(0, 0.5, 0)));
    }

    public boolean upgradeClaim(final Claim claim) {
        // Ignore for levels higher than 6
        if (claim.getType().isUpgradeable() == false)
            return false;
        // ...
        final Claim.Type newType = claim.getType().getNextType();
        // ...
        final ProtectedRegion region = claim.getRegion();
        // ...
        final String id = claim.getRegion().getId();
        final Location center = claim.getCenter();
        // Calculating new region size
        int radius = claim.getType().getNextType().getRadius();
        final BlockVector3 min = BlockVector3.at(center.getBlockX() - radius, center.getWorld().getMinHeight(), center.getBlockZ() - radius);
        final BlockVector3 max = BlockVector3.at(center.getBlockX() + radius, center.getWorld().getMaxHeight(), center.getBlockZ() + radius);
        // Creating cuboid at new points
        final ProtectedRegion newRegion = new ProtectedCuboidRegion(id, min, max);
        // Updating region flag
        region.setFlag(Claims.CustomFlag.CLAIM_TYPE, claim.getType().getNextType().getUniqueId());
        // Redefining region
        newRegion.copyFrom(region);
        regionManager.addRegion(newRegion);
        // Updating Claim with new WorldGuard region
        claim.setRegion(newRegion);
        claim.setType(newType);
        // Updating block type ('& 0xF' thingy is doing some magic to get block's position in chunk)
        final Material type = claim.getType().getBlock().getType();
        PaperLib.getChunkAtAsync(center).thenAccept(chunk -> chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type));
        return true;
    }

    @Override
    public boolean hasClaim(final @NotNull UUID uuid) {
        return this.getClaimPlayer(uuid).getClaim() != null;
    }

    @Override
    public @Nullable Claim getClaim(final @NotNull UUID uuid) {
        return this.getClaimPlayer(uuid).getClaim();
    }

    @Override
    public @Nullable Claim getClaim(final @NotNull String id) {
        return claimsCache.get(id);
    }

    @Override
    public @NotNull ClaimPlayer getClaimPlayer(final @NotNull UUID uuid) {
        return claimPlayerCache.computeIfAbsent(uuid, (u) -> new ClaimPlayer(this, u));
    }

    public @NotNull ClaimPlayer getClaimPlayer(final @NotNull Player player) {
        return claimPlayerCache.computeIfAbsent(player.getUniqueId(), (uuid) -> new ClaimPlayer(this, uuid));
    }

    @Override
    public @NotNull List<String> getClaimIds() {
        return new ArrayList<>(claimsCache.keySet());
    }

    public @NotNull Collection<Claim> getClaims() {
        return Collections.unmodifiableCollection(claimsCache.values());
    }

}

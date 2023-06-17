package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims.CustomFlag;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.paper.math.Position;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;
import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.claims.util.Utilities.getNumberOrDefault;
import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Claim container that stores underlaying {@link ProtectedRegion} instance.
 * Most methods can throw {@link ClaimProcessException} because region can be modified/removed externally using API or commands.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class Claim {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String id;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull ClaimManager manager;

    @Getter(AccessLevel.MODULE) @Setter(value = AccessLevel.MODULE, onMethod = @__({@Internal}))
    private @NotNull ProtectedRegion region;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.MODULE, onMethod = @__({@Internal}))
    private @NotNull Claim.Type type;

    /* FIELDS BELOW ARE EXCLUDED FROM COONSTRUCTOR */

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod = @__({@Internal}))
    private boolean isBeingEdited = false;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod = @__({@Internal}))
    private boolean isPendingRename = false;

    public Location getCenter() throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final com.sk89q.worldedit.util.Location center = region.getFlag(CustomFlag.CLAIM_CENTER);
        // ...
        if (center == null)
            throw new ClaimProcessException(PluginLocale.CLAIM_NO_CENTER_DEFINED);
        // ...
        return BukkitAdapter.adapt(center);
    }

    public Location getHome() throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final com.sk89q.worldedit.util.Location location = region.getFlag(Flags.TELE_LOC);
        // ...
        if (location == null)
            return this.getCenter();
        // ...
        return BukkitAdapter.adapt(location);
    }

    public Long getCreatedOn() throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final String value = requirePresent(region.getFlag(CustomFlag.CLAIM_CREATED), "");
        // ...
        return getNumberOrDefault(() -> Long.parseLong(value), null);
    }

    public @NotNull String getDisplayName() {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // Returns custom name or id if not set.
        return requirePresent(this.getFlag(CustomFlag.CLAIM_NAME), PluginConfig.CLAIM_SETTINGS_DEFAULT_DISPLAY_NAME);
    }

    public boolean setDisplayName(final @NotNull String name) {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final String transformed = name.trim().replace("  ", " ");
        if (inRange(transformed.length(), 1, 32) == true) {
            this.setFlag(CustomFlag.CLAIM_NAME, transformed);
            return true;
        }
        return false;
    }

    public boolean setHome(final Location location) throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        // ...
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) == true) {
            region.setFlag(Flags.TELE_LOC, loc);
            return true;
        }
        return false;
    }

    public <T> @Nullable T getFlag(final @NotNull Flag<T> flag) throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        return region.getFlag(flag);
    }

    public <T> void setFlag(final @NotNull Flag<T> flag, final @Nullable T value) throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        region.setFlag(flag, value);
    }

    public @NotNull List<ClaimPlayer> getOwners() {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        return region.getOwners().getUniqueIds().stream().map(manager::getClaimPlayer).toList();
    }

    public boolean isOwner(final @NotNull ClaimPlayer claimPlayer) throws ClaimProcessException {
        return this.getOwners().stream().anyMatch(claimPlayer::equals);
    }

    public @NotNull List<ClaimPlayer> getMembers() throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        return region.getMembers().getUniqueIds().stream().map(manager::getClaimPlayer).toList();
    }

    public boolean isMember(final @NotNull ClaimPlayer claimPlayer) throws ClaimProcessException {
        return this.getMembers().stream().anyMatch(claimPlayer::equals);
    }

    public boolean addMember(final @NotNull ClaimPlayer member) throws ClaimProcessException {
        if (this.isMember(member) == false) {
            region.getMembers().addPlayer(member.getUniqueId());
            return true;
        }
        return false;
    }

    public boolean removeMember(final @NotNull ClaimPlayer member) throws ClaimProcessException {
        if (this.isMember(member) == true) {
            region.getMembers().removePlayer(member.getUniqueId());
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Claim otherClaim && id.equals(otherClaim.id) == true;
    }

    /* Utility Methods */

    // 'Generates' region ID for specified location
    @SuppressWarnings("UnstableApiUsage")
    public static String createId(final Position position) {
        return new StringBuilder()
                .append(PluginConfig.REGION_PREFIX)
                .append("x").append(position.blockX())
                .append("y").append(position.blockY())
                .append("z").append(position.blockZ())
                .toString();
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public static final class Type {

        @Getter(AccessLevel.PUBLIC)
        private final @NotNull String id;

        @Getter(AccessLevel.PUBLIC)
        private final int radius;

        @Getter(AccessLevel.PUBLIC)
        private final @NotNull ItemStack block;

        @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.MODULE)
        private @Nullable Claim.Type nextType;

        @Getter(AccessLevel.PUBLIC)
        private final @NotNull ItemStack upgradeButton;

        @Getter(AccessLevel.PUBLIC)
        private final @Nullable ItemStack[] upgradeCost;

        public boolean isUpgradeable() {
            return nextType != null;
        }

    }

}

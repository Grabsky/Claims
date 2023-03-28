package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Claim container that stores underlaying {@link ProtectedRegion} instance.
 * Most methods can throw {@link ClaimProcessException} because region can be modified/removed externally using API or commands.
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class Claim {

    @Getter(AccessLevel.PUBLIC)
    private final String id;

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager manager;

    @Getter(AccessLevel.MODULE) @Setter(AccessLevel.MODULE)
    private ProtectedRegion region;

    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.MODULE)
    private Claim.Type type;

    public Location getCenter() throws ClaimProcessException {
        if (manager.containsClaim(this) == false)
            throw new ClaimProcessException(PluginLocale.CLAIM_DOES_NOT_EXIST);
        // ...
        final com.sk89q.worldedit.util.Location center = region.getFlag(Claims.CustomFlag.CLAIM_CENTER);
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
    public static String createId(final Location location) {
        return new StringBuilder()
                .append(PluginConfig.REGION_PREFIX)
                .append("x").append(location.getBlockX())
                .append("y").append(location.getBlockY())
                .append("z").append(location.getBlockZ())
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

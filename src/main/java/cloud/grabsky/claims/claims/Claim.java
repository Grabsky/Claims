package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.eclipse.sisu.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class Claim {

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager manager;

    @Getter(AccessLevel.PUBLIC)
    private final ClaimPlayer owner;

    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.MODULE)
    private ProtectedRegion region;

    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.MODULE)
    private Claim.Type type;

    public Claim(final Claim claim) {
        this(claim.manager, claim.owner, claim.region, claim.type);
    }

    // This shouldn't be null unless manually deleted. Perhaps throw IllegalStateException whenever that happens?
    public Location getCenter() {
        return BukkitAdapter.adapt(region.getFlag(Claims.CustomFlag.CLAIM_CENTER));
    }

    // This shouldn't be null unless manually deleted. Perhaps throw IllegalStateException whenever that happens?
    public Location getHome() {
        return BukkitAdapter.adapt(region.getFlag(Flags.TELE_LOC));
    }

    public boolean setHome(final Location location) {
        final com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            region.setFlag(Flags.TELE_LOC, loc);
            return true;
        }
        return false;
    }

    public @NotNull List<ClaimPlayer> getMembers() {
        return region.getMembers().getUniqueIds().stream().map(manager::getClaimPlayer).toList();
    }

    public boolean isMember(final ClaimPlayer member) {
        return region.getMembers().contains(member.getUniqueId());
    }

    public boolean addMember(final @NotNull ClaimPlayer member) {
        if (this.getMembers().size() < PluginConfig.MEMBERS_LIMIT) {
            final UUID memberUniqueId = member.getUniqueId();
            // ...
            region.getMembers().addPlayer(memberUniqueId);
            return true;
        }
        return false;
    }

    public boolean removeMember(final @NotNull ClaimPlayer member) {
        if (this.isMember(member) == true) {
            region.getMembers().removePlayer(member.getUniqueId());
            return true;
        }
        return false;
    }

    /* Utility Methods */

    // 'Generates' region ID for specified location
    public static String createId(final org.bukkit.Location location) {
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
        private final @NotNull String uniqueId;

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

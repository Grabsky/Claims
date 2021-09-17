package me.grabsky.claims.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.grabsky.claims.Claims;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.flags.ClaimFlags;
import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

public class Claim {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private final String id;
    private final UUID owner;
    private ProtectedRegion wgRegion;

    public Claim(String id, UUID owner, ProtectedRegion wgRegion) {
        this.id = id;
        this.wgRegion = wgRegion;
        this.owner = owner;
    }

    protected void update(ProtectedRegion wgRegion) {
        this.wgRegion = wgRegion;
    }

    public String getId() {
        return id;
    }

    public ProtectedRegion getWGRegion() {
        return wgRegion;
    }

    public UUID getOwner() {
        return owner;
    }

    // This shouldn't be null unless manually deleted
    public int getLevel() {
        return wgRegion.getFlag(ClaimFlags.CLAIM_LEVEL);
    }

    // This shouldn't be null unless manually deleted
    public Location getCenter() {
        return BukkitAdapter.adapt(wgRegion.getFlag(ClaimFlags.CLAIM_CENTER));
    }

    // This shouldn't be null unless manually deleted
    public Location getHome() {
        return BukkitAdapter.adapt(wgRegion.getFlag(Flags.TELE_LOC));
    }

    public boolean setHome(Location location) {
        final com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        if (wgRegion.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            wgRegion.setFlag(Flags.TELE_LOC, loc);
            return true;
        }
        return false;
    }

    public Set<UUID> getMembers() {
        return wgRegion.getMembers().getUniqueIds();
    }

    public boolean isMember(UUID uuid) {
        return wgRegion.getMembers().contains(uuid);
    }

    public boolean addMember(UUID uuid) {
        if (this.getMembers().size() < ClaimsConfig.MEMBERS_LIMIT) {
            wgRegion.getMembers().addPlayer(uuid);
            manager.getClaimPlayer(uuid).addRelative(this.getId());
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID uuid) {
        if (this.getMembers().contains(uuid)) {
            wgRegion.getMembers().removePlayer(uuid);
            manager.getClaimPlayer(uuid).removeRelative(this.getId());
            return true;
        }
        return false;
    }
}

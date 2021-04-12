package net.skydistrict.claims.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import net.skydistrict.claims.ClaimFlags;
import net.skydistrict.claims.configuration.Config;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Set;
import java.util.UUID;

public class Claim {
    private final String id;
    private final ProtectedRegion wgRegion;
    private final UUID owner;

    public Claim(String id, ProtectedRegion wgRegion, UUID owner) {
        this.id = id;
        this.wgRegion = wgRegion;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public ProtectedRegion getWGRegion() {
        return wgRegion;
    }

    // This shouldn't be null unless manually modified
    public int getLevel() {
        return this.wgRegion.getFlag(ClaimFlags.CLAIM_LEVEL);
    }

    // This shouldn't be null unless manually modified
    public Location getCenter() {
        return BukkitAdapter.adapt(this.wgRegion.getFlag(Flags.TELE_LOC));
    }

    public boolean setCenter(Location location) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        if (wgRegion.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            this.wgRegion.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(location));
            return true;
        }
        return false;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return wgRegion.getMembers().getUniqueIds();
    }

    public boolean addMember(UUID uuid) {
        if (this.getMembers().size() < Config.MEMBERS_LIMIT) {
            this.wgRegion.getMembers().addPlayer(uuid);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID uuid) {
        if (this.getMembers().contains(uuid)) {
            this.wgRegion.getMembers().removePlayer(uuid);
            return true;
        }
        return false;
    }

    public boolean upgrade() {
        int level = this.getLevel();
        if (level < 4) {
            Material type = Material.IRON_BLOCK; // TO-DO: Get level-specific type
            Location center = this.getCenter();
            this.wgRegion.setFlag(ClaimFlags.CLAIM_LEVEL, level + 1);
            // Replace block
            PaperLib.getChunkAtAsync(this.getCenter()).thenAccept(chunk -> chunk.getBlock(center.getBlockX(), center.getBlockY(), center.getBlockZ()).setType(type));
            return true;
        }
        return false;
    }

}

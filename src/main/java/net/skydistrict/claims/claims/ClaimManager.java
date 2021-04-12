package net.skydistrict.claims.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.skydistrict.claims.ClaimFlags;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Location;

import java.util.UUID;

// TO-DO: Make it work with ClaimCache or merge 'em together
public class ClaimManager {
    private final Claims instance;
    private final RegionManager regionManager;

    public ClaimManager(Claims instance) {
        this.instance = instance;
        this.regionManager = instance.getRegionManager();
    }

    public boolean createRegionAt(Location loc, UUID owner) {
        // Points
        BlockVector3 min = BlockVector3.at(loc.getBlockX() - 15, 0, loc.getBlockZ() - 15);
        BlockVector3 max = BlockVector3.at(loc.getBlockX() + 15, 255, loc.getBlockZ() + 15);
        // Checking if there is no region at the given points
        if (regionManager.getApplicableRegions(min).size() != 0 || regionManager.getApplicableRegions(max).size() != 0) return false;
        // Creating region id
        String id = ClaimH.createId(loc);
        // Creating region at new points
        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        // Setting default flags
        this.setDefaultFlags(region, loc);
        // Adding owner
        region.getOwners().addPlayer(owner);
        // Registering region
        regionManager.addRegion(region);
        // TO-DO: Cache newly created province
        return true;
    }

    // Existence check is already in RegionHandler
    public void removeRegionWithId(String id) {
        regionManager.removeRegion(id);
    }

    private void upgrade(Claim land) {
        ProtectedRegion wgRegion = land.getWGRegion();
        String id = land.getId();
        // Min point
        BlockVector3 min = wgRegion.getMinimumPoint();
        BlockVector3 newMin = BlockVector3.at(min.getBlockX() - 5, min.getBlockY(), min.getBlockZ() - 5);
        // Max point
        BlockVector3 max = wgRegion.getMaximumPoint();
        BlockVector3 newMax = BlockVector3.at(max.getBlockX() + 5, max.getBlockY(), max.getBlockZ() + 5);
        // Creating cuboid at new points
        ProtectedRegion newRegion = new ProtectedCuboidRegion(id, newMin, newMax);
        // Redefining region
        newRegion.copyFrom(wgRegion);
        regionManager.addRegion(newRegion);
        // TO-DO: Cache newly created land
    }

    private void setDefaultFlags(ProtectedRegion region, Location loc) {
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.SNOW_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.ICE_MELT, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc));
        region.setFlag(ClaimFlags.CLAIM_LEVEL, 0);
    }

}
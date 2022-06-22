package claims.plugin.claims

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import java.util.*

object Claims {
    fun id(location: org.bukkit.Location): String {
        return buildString {
            append("PREFIX")
            append("x${location.blockX}")
            append("y${location.blockX}")
            append("z${location.blockX}")
        }
    }
}

class Claim(val id: String, val owner: UUID, var region: ProtectedRegion) {

    fun getHome(): org.bukkit.Location? {
        return BukkitAdapter.adapt(region.getFlag(Flags.TELE_LOC))
    }

    fun setHome(location: org.bukkit.Location): Boolean {
        // Returning 'false' if provided location is not inside region
        if (region.contains(location.blockX, location.blockY, location.blockZ) == false) return false
        // Creating WorldEdit's Location object from org.bukkit.Location
        val notBukkitLocation = BukkitAdapter.adapt(location)
        // Setting 'teleport-location' flag
        region.setFlag(Flags.TELE_LOC, notBukkitLocation)
        // Returning 'true' after operation completion
        return true
    }

    fun getMembers(): Set<UUID> {
        return region.members.uniqueIds
    }

    fun isMember(uuid: UUID): Boolean {
        return region.members.uniqueIds.contains(uuid)
    }

    fun addMember(uuid: UUID): Boolean {
        // Returning 'false' if region reached members limit
        if (this.getMembers().size >= 10) return false
        // Adding player to the region
        region.members.addPlayer(uuid)
        // Returning 'true' after operation completion
        return true
    }

    fun removeMember(uuid: UUID): Boolean {
        // Returning 'false' if provided player is not a claim member
        if (this.isMember(uuid) == false) return false
        // Removing player from the region
        region.members.removePlayer(uuid)
        // Returning 'true' after operation completion
        return true
    }

    // TO-DO...
    fun upgrade(): Boolean {
        return true
    }
}
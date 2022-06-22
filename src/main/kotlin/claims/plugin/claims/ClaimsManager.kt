package claims.plugin.claims

import claims.framework.ClaimsManagerAPI
import claims.plugin.Claims
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import org.bukkit.Bukkit

class ClaimsManager(claims: Claims) : ClaimsManagerAPI {

    private val regionManager: RegionManager

    init {
        val world = BukkitAdapter.adapt(Bukkit.getWorlds()[0]) // This world is placeholder
        this.regionManager = WorldGuard.getInstance().platform.regionContainer[world] ?: throw TODO()
    }

}
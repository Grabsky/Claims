package claims.plugin

import claims.framework.ClaimsAPI
import indigo.framework.ServerPlugin
import indigo.framework.logger.FileLogger
import indigo.framework.utils.DateFormats
import org.bukkit.NamespacedKey
import java.io.File

object ClaimsKeys {
    val CLAIM_LEVEL: NamespacedKey = NamespacedKey("claims", "claim_level")
}

object ClaimsProvider {
    internal lateinit var INS: Claims
    lateinit var API: ClaimsAPI
}

class Claims : ServerPlugin(), ClaimsAPI {
    lateinit var fileLogger: FileLogger

    override fun onEnable() {
        super.onEnable()
        // Creating instances
        ClaimsProvider.INS = this
        ClaimsProvider.API = this
        // Setting up logger
        this.fileLogger = FileLogger(this, File(this.dataFolder, "logs.log"), DateFormats.UNIVERSAL)
        // Setting up configuration files
        this.reloadConfiguration()
    }

    override fun reloadPlugin(): Boolean {
        TODO("Not yet implemented")
    }

    private fun reloadConfiguration(): Boolean {
        return false
    }

}
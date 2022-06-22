package claims.plugin.claims

import java.util.*

class ClaimsPlayer(val uuid: UUID) {
    var claim: Claim? = null
    private val relatives: MutableSet<String> = mutableSetOf()

    fun getRelatives(): Set<String> {
        return this.relatives
    }

    protected fun addRelative(regionId: String) {
        relatives.add(regionId)
    }

    protected fun removeRelative(regionId: String) {
        relatives.remove(regionId)
    }
}
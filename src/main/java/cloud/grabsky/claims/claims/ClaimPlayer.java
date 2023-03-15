package cloud.grabsky.claims.claims;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimPlayer {
    private final UUID uuid;
    private Claim claim;
    private final Set<String> relatives;

    public ClaimPlayer(UUID uuid) {
        this.uuid = uuid;
        this.claim = null;
        this.relatives = new HashSet<>();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean hasClaim() {
        return claim != null;
    }

    public Claim getClaim() {
        return claim;
    }

    protected void setClaim(Claim claim) {
        this.claim = claim;
    }

    public boolean hasRelatives() {
        return !relatives.isEmpty();
    }

    public Set<String> getRelatives() {
        return relatives;
    }

    protected void addRelative(String id) {
        relatives.add(id);
    }

    protected void removeRelative(String id) {
        relatives.remove(id);
    }

}
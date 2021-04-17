package net.skydistrict.claims.claims;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimPlayer {
    private final UUID uuid;
    private Claim claim;
    private final Set<String> relatives;

    public ClaimPlayer(UUID uuid, @Nullable Claim claim, Set<String> relatives) {
        this.uuid = uuid;
        this.claim = claim;
        this.relatives = relatives;
    }

    public ClaimPlayer(UUID uuid) {
        this.uuid = uuid;
        this.claim = null;
        this.relatives = new HashSet<>();
    }

    public boolean hasClaim() {
        return (claim != null);
    }

    public Claim getClaim() {
        return claim;
    }

    protected void setClaim(Claim claim) {
        this.claim = claim;
    }

    public boolean hasRelatives() {
        return this.relatives != null && !this.relatives.isEmpty();
    }

    public Set<String> getRelatives() {
        return relatives;
    }

    protected void addRelative(String id) {
        this.relatives.add(id);
    }

    protected void removeRelative(String id) {
        this.relatives.remove(id);
    }

}
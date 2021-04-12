package net.skydistrict.claims.claims;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimPlayer {
    private final UUID uuid;
    private final Claim claim;

    public ClaimPlayer(UUID uuid, @Nullable Claim claim) {
        this.uuid = uuid;
        this.claim = claim;
    }

    public boolean hasClaim() {
        return (claim != null);
    }

    public Claim getLand() {
        return claim;
    }
}

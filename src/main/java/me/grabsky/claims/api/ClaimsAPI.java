package me.grabsky.claims.api;

import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ClaimsAPI {

    /**
     * @param uuid Player's UUID
     * @return Returns true if player has claim
     */
    boolean hasClaim(UUID uuid);

    /**
     * @param uuid Player's UUID
     * @return Returns Claim if player owns one
     */
    @Nullable
    Claim getClaim(UUID uuid);

    /**
     * @param id Claim ID
     * @return Returns Claim if exists
     */
    @Nullable
    Claim getClaim(String id);

    /**
     * @param uuid Player's UUID
     * @return Returns ClaimPlayer
     */
    @NotNull
    ClaimPlayer getClaimPlayer(UUID uuid);

    /**
     * @return Returns list of claims
     */
    @NotNull
    List<String> getClaimIds();
}

package net.skydistrict.claims.api;

import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.claims.ClaimPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimsAPI {
    private static final ClaimManager manager = Claims.getInstance().getClaimManager();

    public static boolean hasClaim(UUID uuid) {
        return manager.getClaimPlayer(uuid).hasClaim();
    }

    @Nullable
    public static Claim getClaim(UUID uuid) {
        return manager.getClaimPlayer(uuid).getClaim();
    }

    @Nullable
    public static Claim getClaim(String id) {
        return manager.getClaim(id);
    }

    @NotNull
    public static ClaimPlayer getClaimPlayer(UUID uuid) {
        return manager.getClaimPlayer(uuid);
    }
}

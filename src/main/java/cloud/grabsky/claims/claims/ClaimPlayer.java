package cloud.grabsky.claims.claims;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimPlayer {

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager manager;

    @Getter(AccessLevel.PUBLIC)
    private final UUID uniqueId;

    @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.MODULE)
    private Claim claim;

    public boolean hasClaim() {
        return claim != null;
    }

    public List<Claim> getRelativeClaims() {
        return manager.getClaims().stream().filter(this::isMemberOf).toList();
    }

    public boolean isMemberOf(final Claim claim) {
        return claim.isMember(this);
    }

    public boolean isOwnerOf(final Claim claim) {
        return claim.getOwner() == this;
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public User toUser() {
        return AzureProvider.getAPI().getUserCache().getUser(uniqueId);
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ClaimPlayer otherClaimPlayer && uniqueId.equals(otherClaimPlayer.uniqueId) == true);
    }
}
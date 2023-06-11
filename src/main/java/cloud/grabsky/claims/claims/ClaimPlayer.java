package cloud.grabsky.claims.claims;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimPlayer {

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager claimManager;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull UUID uniqueId;

    @Internal @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC)
    private boolean isChangingClaimName = false;

    /**
     * Returns {@code true} if (this) {@link ClaimPlayer} is owner of <i>any</i> {@link Claim}.
     */
    public boolean hasClaim() {
        return this.getClaims().isEmpty() == false;
    }

    // ...
    public Set<Claim> getClaims() {
        return claimManager.getClaims().stream().filter(this::isOwnerOf).collect(Collectors.toUnmodifiableSet());
    }

    public Set<Claim> getRelativeClaims() {
        return claimManager.getClaims().stream().filter(this::isMemberOf).collect(Collectors.toSet());
    }

    /**
     * Returns {@code true} if (this) {@link ClaimPlayer} is member of provided {@link Claim}.
     */
    public boolean isMemberOf(final Claim claim) {
        return claim.isMember(this);
    }

    /**
     * Returns {@code true} if (this) {@link ClaimPlayer} is owner of provided {@link Claim}.
     */
    public boolean isOwnerOf(final Claim claim) {
        return claim.isOwner(this);
    }

    /**
     * Returns {@link Player} instance relative to (this) {@link ClaimPlayer}.
     */
    public Player toPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    /**
     * Returns {@link User} instance relative to this {@link ClaimPlayer}.
     */
    public User toUser() {
        return AzureProvider.getAPI().getUserCache().getUser(uniqueId);
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ClaimPlayer otherClaimPlayer && uniqueId.equals(otherClaimPlayer.uniqueId) == true);
    }
}
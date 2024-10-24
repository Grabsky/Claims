/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims.claims;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents a {@link Player} that can be associated with a {@link Claim}.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimPlayer {

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager claimManager;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull UUID uniqueId;

    @Getter(value = AccessLevel.PUBLIC, onMethod_ = @Internal)
    private final @NotNull Set<Integer> borderEntities = new HashSet<>();

    @Internal @Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC)
    private boolean isChangingClaimName = false;

    /**
     * Returns {@code true} if this {@link ClaimPlayer} is owner of any {@link Claim}.
     */
    public boolean hasClaim() {
        return this.getClaims().isEmpty() == false;
    }

    /**
     * Returns list of {@link Claim Claims} this {@link ClaimPlayer} is owner of.
     */
    public Set<Claim> getClaims() {
        return claimManager.getClaims().stream().filter(this::isOwnerOf).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns list of {@link Claim Claims} this {@link ClaimPlayer} is member of.
     */
    public Set<Claim> getRelativeClaims() {
        return claimManager.getClaims().stream().filter(this::isMemberOf).collect(Collectors.toSet());
    }

    /**
     * Returns {@code true} if this {@link ClaimPlayer} is member of provided {@link Claim}.
     */
    public boolean isMemberOf(final Claim claim) {
        return claim.isMember(this);
    }

    /**
     * Returns {@code true} if this {@link ClaimPlayer} is owner of provided {@link Claim}.
     */
    public boolean isOwnerOf(final Claim claim) {
        return claim.isOwner(this);
    }

    /**
     * Returns {@link Player} instance relative to this {@link ClaimPlayer}.
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
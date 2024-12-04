/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.claims;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Returns number of {@link Claim Claims} this {@link ClaimPlayer} can have. Defaults to {@code claim_settings.claims_limit} configuration value and returns {@code -1} for offline players.
     */
    public int getClaimsLimit() {
        final Player player = this.toPlayer();
        // Returning '-1' if player is not online.
        if (player == null || player.isOnline() == false)
            return -1;
        // Iterating over 'claims.plugin.claims_limit' permissions and returning the highest number found. Defaults to a config value.
        return player.getEffectivePermissions().stream()
                // Including only permissions that start with 'claims.plugin.claims_limit.'
                .filter(it -> it.getValue() == true && it.getPermission().startsWith("claims.plugin.claims_limit.") == true)
                // Unboxing the number from permission.
                .map(it -> {
                    final @Nullable Integer value = Utilities.parseInt(it.getPermission().replace("claims.plugin.claims_limit.", ""));
                    return (value != null && value >= 0) ? value : null;
                })
                // Filtering 'null' values.
                .filter(Objects::nonNull)
                // Returning the maximum number in this stream.
                .mapToInt(Integer::intValue).max().orElse(PluginConfig.CLAIM_SETTINGS_CLAIMS_LIMIT);
    }

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
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
package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerListener implements Listener {

    private final @NotNull Claims plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerQuitEvent event) {
        // Getting UUID of the player associated with this event.
        final UUID player = event.getPlayer().getUniqueId();
        // Clearing border entities, as these are no longer visible.
        plugin.getClaimManager().getClaimPlayer(player).getBorderEntities().clear();
    }

}

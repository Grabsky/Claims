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

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        // Skipping if player has bypass permission.
        if (player.hasPermission("claims.plugin.bypass_blocked_commands") == true)
            return;
        // Getting executed command.
        String baseCommand = event.getMessage().substring(1).split("\\s+")[0].toLowerCase();
        // Stripping namespace from the command.
        if (baseCommand.contains(":") == true)
            baseCommand = baseCommand.split(":", 2)[1];
        // Continuing only if command is in the list.
        if (PluginConfig.BLOCKED_COMMANDS_SETTINGS_BLOCKED_COMMANDS.contains(baseCommand) == true) {
            final @Nullable RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
            // Continuing only with RegionManager instance. If it's somehow null, commands will be denied.
            if (regionManager != null) {
                // Getting applicable regions at player's location.
                final ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
                // Iterating over applicable regions.
                for (final ProtectedRegion region : regions) {
                    final String id = region.getId();
                    // Skipping further logic if region is in the list of allowed regions.
                    if (PluginConfig.BLOCKED_COMMANDS_SETTINGS_ALLOW_IN_REGIONS.contains(id) == true)
                        return;
                    // Authorized / unauthorized region check.
                    if (id.startsWith(PluginConfig.REGION_PREFIX) == true) {
                        // Getting player's unique identifier.
                        final UUID uniqueId = player.getUniqueId();
                        // Skipping further logic if inside an authorized claim.
                        if (PluginConfig.BLOCKED_COMMANDS_SETTINGS_ALLOW_ON_AUTHORIZED_CLAIMS == true)
                            if (region.getOwners().getUniqueIds().contains(uniqueId) == true || region.getMembers().getUniqueIds().contains(uniqueId) == true)
                                return;
                        // Skipping further logic if inside an unauthorized claim.
                        if (PluginConfig.BLOCKED_COMMANDS_SETTINGS_ALLOW_ON_UNAUTHORIZED_CLAIMS == true)
                            return;
                    }
                }
            }
            event.setCancelled(true);
            Message.of(PluginLocale.COMMAND_CANNOT_BE_USED_HERE).placeholder("input", baseCommand).send(player);
        }
    }

}

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
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.util.Utilities;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import cloud.grabsky.claims.waypoints.WaypointManager;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WaypointListener implements Listener {

    private final Claims plugin;

    private final ClaimManager claimManager;
    private final WaypointManager waypointManager;

    public WaypointListener(final Claims plugin) {
        this.plugin = plugin;
        this.claimManager = plugin.getClaimManager();
        this.waypointManager = plugin.getWaypointManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointPlace(final @NotNull BlockPlaceEvent event) {
        if (event.canBuild() == false)
            return;
        // Checking of feature is enabled and if placed block is LODESTONE
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == true && event.getBlockPlaced().getType() == Material.LODESTONE) {
            final Player player = event.getPlayer();
            // Getting the maximum number of waypoints this player can have.
            final int limit = waypointManager.getWaypointsLimit(player);
            // Checking if player has not reached the maximum number of waypoints.
            if (player.hasPermission("claims.bypass.ignore_waypoints_limit") == true || waypointManager.getWaypoints(player).stream().filter(waypoint -> waypoint.getSource() == Source.BLOCK).count() < limit) {
                final Location location = event.getBlock().getLocation().toCenterLocation();
                // Trying to create the waypoint.
                this.create(player, location).thenAccept(isSuccess -> {
                    // Sending success message to the player.
                    if (isSuccess == true)
                        Message.of(PluginLocale.WAYPOINT_PLACE_SUCCESS).replace("<limit>", String.valueOf(limit)).send(player);
                });
                return;
            }
            // Cancelling the event otherwise.
            event.setCancelled(true);
            // Sending error message to the player.
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_REACHED_WAYPOINTS_LIMIT).placeholder("limit", limit).send(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointInteract(final @NotNull PlayerInteractEvent event) {
        // Skipping non-right-click-block actions and cancelled actions.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY)
            return;
        // ...
        final @Nullable Block block = event.getClickedBlock();
        // ...
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == true && block != null && block.getType() == Material.LODESTONE) {
            // ...
            final Location location = block.getLocation().toCenterLocation();
            // ...
            final @Nullable Waypoint waypoint = waypointManager.getBlockWaypoint(location);
            // Returning if waypoint at this location exists and player is not it's owner.
            if (Utilities.findFirstBlockUnder(location, 5, Material.COMMAND_BLOCK) != null || (waypoint != null && waypoint.getOwner().equals(event.getPlayer().getUniqueId()) == true) == true) {
                // Skipping when player is trying to place a block.
                if (event.getPlayer().isSneaking() == true && (isBlockOrSummonsEntity(event.getPlayer().getInventory().getItemInMainHand()) == true || isBlockOrSummonsEntity(event.getPlayer().getInventory().getItemInMainHand())) == true)
                    return;
                // Cancelling the click.
                event.setCancelled(true);
                // Make sure to handle interaction only for the main hand.
                if (event.getHand() != EquipmentSlot.HAND)
                    return;
                // Opening the panel.
                new ClaimPanel.Builder()
                        .setClaimManager(claimManager)
                        .setAccessBlockLocation(location)
                        .build().open(event.getPlayer(), (panel) -> {
                            plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                            return true;
                        });
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointBreak(final @NotNull BlockBreakEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false || event.getBlock().getType() != Material.LODESTONE)
            return;
        // ...
        final Location location = event.getBlock().getLocation().toCenterLocation();
        // ...
        final @Nullable Waypoint waypoint = waypointManager.getBlockWaypoint(location);
        // ...
        if (waypoint != null) {
            // Removing drops and experience.
            event.setDropItems(false);
            event.setExpToDrop(0);
            // Destroying...
            this.destroy(waypoint.getOwner(), location);
            // ...
            return;
        }
        plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockExplode(final @NotNull BlockExplodeEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false)
            return;
        // Removing entries from this list, makes them NOT BEING DESTROYED by the explosion.
        event.blockList().removeIf(block -> {
            // ...
            if (block.getType() == Material.LODESTONE) {
                final Location location = block.getLocation().toCenterLocation();
                // ...
                final @Nullable Waypoint waypoint = waypointManager.getBlockWaypoint(location);
                // ...
                if (waypoint != null) {
                    // Setting block to AIR, to remove drops.
                    block.setType(Material.AIR);
                    // Destroying...
                    this.destroy(waypoint.getOwner(), location);
                    // ...
                    return false;
                }
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
            }
            // ...
            return false;
        });
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityExplode(final @NotNull EntityExplodeEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false)
            return;
        // Removing entries from this list, makes them NOT BEING DESTROYED by the explosion.
        event.blockList().removeIf(block -> {
            // ...
            if (block.getType() == Material.LODESTONE) {
                final Location location = block.getLocation().toCenterLocation();
                // ...
                final @Nullable Waypoint waypoint = waypointManager.getBlockWaypoint(location);
                // ...
                if (waypoint != null) {
                    // Setting block to AIR, to remove drops.
                    block.setType(Material.AIR);
                    // Destroying...
                    this.destroy(waypoint.getOwner(), location);
                    // ...
                    return false;
                }
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
            }
            // ...
            return false;
        });
    }

    private CompletableFuture<Boolean> create(final @NotNull Player owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(owner.getUniqueId(), PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // Creating the waypoints.
        return waypoint.create(waypointManager).thenApply(isSuccess -> {
            // Setting cooldown to prevent block place spam. Unfortunately this works per-material and not per-item.
            owner.setCooldown(Material.LODESTONE, PluginConfig.WAYPOINT_SETTINGS_PLACE_COOLDOWN * 20);
            // Passing returned 'isSuccess' variable to the next stage.
            return isSuccess;
        });
    }

    private void destroy(final @NotNull UUID owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(owner, PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // ...
        waypoint.destroy(waypointManager);
    }

    // Likely to be incomplete.
    private static boolean isBlockOrSummonsEntity(final @NotNull ItemStack item) {
        return item.getType().isBlock() == true
                || item.getType() == Material.ARMOR_STAND
                || item.getType() == Material.ITEM_FRAME
                || item.getType() == Material.GLOW_ITEM_FRAME
                || MaterialTags.SPAWN_EGGS.isTagged(item)
                || Tag.ITEMS_BOATS.isTagged(item.getType());
    }

}

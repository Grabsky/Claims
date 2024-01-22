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
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.UUID;

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
        // ...
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == true && event.getBlockPlaced().getType() == Material.LODESTONE) {
            final Player player = event.getPlayer();
            // ...
            if (waypointManager.getWaypoints(player).stream().filter(waypoint -> waypoint.getSource() == Source.BLOCK).count() < PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS_LIMIT || player.hasPermission("claims.bypass.ignore_waypoints_limit") == true) {
                final Location location = event.getBlock().getLocation().toCenterLocation();
                // Trying to create the waypoint.
                this.create(player, location);
                // ...
                return;
            }
            event.setCancelled(true);
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_REACHED_WAYPOINTS_LIMIT).placeholder("limit", PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS_LIMIT).send(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.getItem() != null || event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY)
            return;
        // ...
        final @Nullable Block block = event.getClickedBlock();
        // ...
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == true && block != null && block.getType() == Material.LODESTONE) {
            // ...
            final Location location = block.getLocation().toCenterLocation();
            // ...
            final @Nullable Waypoint waypoint = waypointManager.getBlockWaypoint(location);
            // ...
            if (waypoint == null)
                return;
            // Checking whether player can access this waypoint.
            if (waypoint.getOwner().equals(event.getPlayer().getUniqueId()) == true || Utilities.findFirstBlockUnder(location, 5, Material.COMMAND_BLOCK) != null) {
                // Cancelling the click.
                event.setCancelled(true);
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

    private void create(final @NotNull Player owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(owner.getUniqueId(), PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // ...
        waypoint.create(waypointManager).thenAccept(___ -> {
            // Setting cooldown to prevent block place spam. Unfortunately this works per-material and not per-itemstack.
            owner.setCooldown(Material.LODESTONE, PluginConfig.WAYPOINT_SETTINGS_PLACE_COOLDOWN * 20);
        });
    }

    private void destroy(final @NotNull UUID owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(owner, PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // ...
        waypoint.destroy(waypointManager);
    }

}

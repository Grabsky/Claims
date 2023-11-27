package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import cloud.grabsky.claims.waypoints.WaypointManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static cloud.grabsky.claims.util.Utilities.toChunkPosition;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkDataKey;

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
                // ...
                final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
                // Trying to create the waypoint.
                this.create(key, player, location).whenComplete((isSuccess, e) -> {
                    // Printing stack trace in case some exception occurred during the method invocation.
                    if (e != null) e.printStackTrace();
                    // TO-DO: Error message suggesting to destroy and place one more time. (?)
                });
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
            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // Preventing non-owners from opening the menu.
            if (location.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "INVALID_UUID").equals(event.getPlayer().getUniqueId().toString()) == false) {
                // TEMPORRARY; OPENING GLOBAL WAYPOINT PANEL
                for (int y = block.getY(); y >= block.getY() - 5; y--) {
                    if (block.getWorld().getBlockAt(block.getX(), y, block.getZ()).getType() == Material.COMMAND_BLOCK) {
                        // CANCELLING THE INTERACTION
                        event.setCancelled(true);
                        // OPENING
                        new ClaimPanel.Builder()
                                .setClaimManager(claimManager)
                                .setAccessBlockLocation(location)
                                .build().open(event.getPlayer(), (panel) -> {
                                    plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                                    return true;
                                });
                        break;
                    }
                }
                return;
            }
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

    @EventHandler(ignoreCancelled = true)
    public void onWaypointBreak(final @NotNull BlockBreakEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false || event.getBlock().getType() != Material.LODESTONE)
            return;
        // ...
        final Location location = event.getBlock().getLocation().toCenterLocation();
        final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
        // ...
        if (location.getChunk().getPersistentDataContainer().has(key, PersistentDataType.STRING) == true) {
            // Removing drops and experience.
            event.setDropItems(false);
            event.setExpToDrop(0);
            // IllegalArgumentException is thrown when provided UUID is invalid.
            final UUID ownerUniqueId = UUID.fromString(location.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "INVALID_UUID"));
            // Getting first waypoint that matches specified location.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(ownerUniqueId, (cached) -> {
                return cached.getLocation().complete() != null && cached.getLocation().complete().equals(location) == true;
            });
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(ownerUniqueId, waypoint).whenComplete((isSuccess, e) -> {
                // Printing stack trace in case some exception occurred during the method invocation.
                if (e != null) e.printStackTrace();
                // TO-DO: Error message suggesting to destroy and place one more time. (?)
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockExplode(final @NotNull BlockExplodeEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false)
            return;
        // Removing entries from this list, makes them NOT BEING DESTROYED by the explosion.
        event.blockList().removeIf(block -> {
            // Skippin
            if (block.getType() != Material.LODESTONE)
                return false;
            // ...
            final Location location = block.getLocation().toCenterLocation();
            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // Skipping blocks that are
            if (location.getChunk().getPersistentDataContainer().has(key, PersistentDataType.STRING) == false)
                return false;
            // IllegalArgumentException is thrown when provided UUID is invalid.
            final UUID ownerUniqueId = UUID.fromString(location.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "INVALID_UUID"));
            //
            // Getting first waypoint that matches specified location.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(ownerUniqueId, (cached) -> {
                return cached.getLocation().complete() != null && cached.getLocation().complete().equals(location) == true;
            });
            // Setting block to AIR, to remove drops.
            block.setType(Material.AIR);
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return false;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(ownerUniqueId, waypoint).whenComplete((isSuccess, e) -> {
                // Printing stack trace in case some exception occurred during the method invocation.
                if (e != null) e.printStackTrace();
            });
            return false;
        });
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityExplode(final @NotNull EntityExplodeEvent event) {
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == false)
            return;
        // Removing entries from this list, makes them NOT BEING DESTROYED by the explosion.
        event.blockList().removeIf(block -> {
            // Skippin
            if (block.getType() != Material.LODESTONE)
                return false;
            // ...
            final Location location = block.getLocation().toCenterLocation();
            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // Skipping blocks that are
            if (location.getChunk().getPersistentDataContainer().has(key, PersistentDataType.STRING) == false)
                return false;
            // IllegalArgumentException is thrown when provided UUID is invalid.
            final UUID owner = UUID.fromString(location.getChunk().getPersistentDataContainer().getOrDefault(toChunkDataKey(toChunkPosition(location)), PersistentDataType.STRING, "INVALID_UUID"));
            // Getting first waypoint that matches specified location.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(owner, (cached) -> {
                return cached.getLocation().complete() != null && cached.getLocation().complete().equals(location) == true;
            });
            // Setting block to AIR, to remove drops.
            block.setType(Material.AIR);
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return false;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(owner, waypoint);
            return false;
        });
    }

    private void create(final @NotNull Player owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // ...
        waypoint.create(waypointManager, owner.getUniqueId()).thenAccept(___ -> {
            // Setting cooldown to prevent block place spam. Unfortunately this works per-material and not per-itemstack.
            owner.setCooldown(Material.LODESTONE, PluginConfig.WAYPOINT_SETTINGS_PLACE_COOLDOWN * 20);
        });
    }

    private void destroy(final @NotNull Player owner, final @NotNull Location location) {
        final Waypoint waypoint = Waypoint.fromBlock(PluginConfig.WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME, location);
        // ...
        waypoint.destroy(waypointManager, owner.getUniqueId());
    }

}

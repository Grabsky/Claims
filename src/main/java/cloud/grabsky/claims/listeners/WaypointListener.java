package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import cloud.grabsky.claims.waypoints.WaypointManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkDataKey;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkPosition;

public final class WaypointListener implements Listener {

    private final Claims plugin;

    private final ClaimManager claimManager;
    private final WaypointManager waypointManager;

    private static final Color TRANSPARENT = Color.fromARGB(0, 0, 0, 0);

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
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_REACHED_WAYPOINTS_LIMIT).send(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.getItem() != null || event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY)
            return;
        // ...
        final @Nullable Block block = event.getClickedBlock();
        //  ...
        if (PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS == true && block != null && block.getType() == Material.LODESTONE) {
            final Location location = block.getLocation().toCenterLocation();
            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // Preventing non-owners from opening the menu.
            if (location.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "INVALID_UUID").equals(event.getPlayer().getUniqueId().toString()) == false)
                return;
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
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(ownerUniqueId, (cached) -> cached.getLocation().equals(location) == true);
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(key, ownerUniqueId, waypoint).whenComplete((isSuccess, e) -> {
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
            // Getting first waypoint that matches specified location.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(ownerUniqueId, (cached) -> cached.getLocation().equals(location) == true);
            // Setting block to AIR, to remove drops.
            block.setType(Material.AIR);
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return false;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(key, ownerUniqueId, waypoint).whenComplete((isSuccess, e) -> {
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
            final UUID ownerUniqueId = UUID.fromString(location.getChunk().getPersistentDataContainer().getOrDefault(key, PersistentDataType.STRING, "INVALID_UUID"));
            // Getting first waypoint that matches specified location.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(ownerUniqueId, (cached) -> cached.getLocation().equals(location) == true);
            // Setting block to AIR, to remove drops.
            block.setType(Material.AIR);
            // Returning if no waypoint has been found in that location.
            if (waypoint == null) {
                plugin.getLogger().warning("Lodestone destroyed at [" + location.x() + ", " + location.y() + ", " + location.z() + " in " + location.getWorld() + "] but no waypoint has been found at this location.");
                return false;
            }
            // Trying to "destroy" the waypoint.
            this.destroy(key, ownerUniqueId, waypoint).whenComplete((isSuccess, e) -> {
                // Printing stack trace in case some exception occurred during the method invocation.
                if (e != null) e.printStackTrace();
            });
            return false;
        });
    }

    private CompletableFuture<Boolean> create(final @NotNull NamespacedKey key, final @NotNull Player owner, final @NotNull Location location) {
        final String name = Waypoint.createDefaultName(location);
        // Trying to create the waypoint...
        return waypointManager.createWaypoint(owner.getUniqueId(), name, Source.BLOCK, location).thenApply(isSuccess -> {
            // Returning 'false' as soon as creation failed.
            if (isSuccess == false) return false;
            // This stuff have to be scheduled onto the main thread.
            plugin.getBedrockScheduler().run(1L, (task) -> {
                location.getBlock().getChunk().getPersistentDataContainer().set(key, PersistentDataType.STRING, owner.getUniqueId().toString());
                // Playing place sound.
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
                // Setting cooldown to prevent block place spam. Unfortunately this works per-material and not per-itemstack.
                owner.setCooldown(Material.LODESTONE, PluginConfig.WAYPOINT_SETTINGS_PLACE_COOLDOWN * 20);
                // Creating TextDisplay above placed block.
                location.getWorld().spawnEntity(location.clone().add(0F, 0.75F, 0F), EntityType.TEXT_DISPLAY, SpawnReason.CUSTOM, (entity) -> {
                    if (entity instanceof TextDisplay display) {
                        // Setting PDC to easily distinguish from other entities.
                        display.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                        // Setting other visual properties.
                        display.text(owner.name());
                        display.setBillboard(Display.Billboard.CENTER);
                        display.setShadowed(true);
                        display.setBackgroundColor(TRANSPARENT); // DRAFT API; NOTHING TO WORRY ABOUT
                        display.setViewRange(0.2F);
                    }
                });
            });
            return true;
        });
    }

    private CompletableFuture<Boolean> destroy(final @NotNull NamespacedKey key, final @NotNull UUID uniqueId, final @NotNull Waypoint waypoint) {
        // Invalidating sessions and closing inventories.
        Bukkit.getOnlinePlayers().forEach((player) -> {
            final UUID onlineUniqueId = player.getUniqueId();
            final @Nullable Session<?> session = Session.Listener.CURRENT_EDIT_SESSIONS.getIfPresent(onlineUniqueId);
            if (session != null) {
                final @Nullable Location sessionAccessBlockLocation = session.getAssociatedPanel().getAccessBlockLocation();
                // Skipping unrelated sessions.
                if (sessionAccessBlockLocation != null && (waypoint.getLocation().equals(sessionAccessBlockLocation) == true || session.getSubject().equals(waypoint) == true)) {
                    final @Nullable Player sessionOperator = Bukkit.getPlayer(onlineUniqueId);
                    // Invalidating and clearing the title.
                    if (sessionOperator != null && sessionOperator.isOnline() == true) {
                        Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(onlineUniqueId);
                        sessionOperator.clearTitle();
                    }
                }
            }
            // Closing open panels.
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel cPanel) {
                if (waypoint.getLocation().equals(cPanel.getAccessBlockLocation()) == true)
                    player.closeInventory();
            }
        });
        // Trying to remove the waypoint...
        return waypointManager.removeWaypoint(uniqueId, waypoint).thenApply(isSuccess -> {
            // Returning 'false' as soon as removal failed.
            if (isSuccess == false) return false;
            // This stuff have to be scheduled onto the main thread.
            plugin.getBedrockScheduler().run(1L, (task) -> {
                final Location location = waypoint.getLocation();
                // ...
                location.getChunk().getPersistentDataContainer().remove(key);
                // Displaying visual effects.
                location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                // Removing TextDisplay above the block.
                location.getNearbyEntities(2, 2, 2).stream().filter(TextDisplay.class::isInstance).forEach(entity -> {
                    if (entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE) == true)
                        entity.remove();
                });
            });
            return true;
        });
    }

}

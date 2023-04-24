package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.views.TeleportView;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class WaypointListener implements Listener {

    private final Claims plugin;
    private final WaypointManager waypointManager;

    public static Material WAYPOINT_BLOCK_TYPE = PluginConfig.WAYPOINT_BLOCK.getType();

    public WaypointListener(final Claims plugin) {
        this.plugin = plugin;
        this.waypointManager = plugin.getWaypointManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointPlace(final @NotNull BlockPlaceEvent event) {
        if (event.canBuild() == false)
            return;
        // ...
        if (event.getBlockPlaced().getType() == WAYPOINT_BLOCK_TYPE) {
            final Player player = event.getPlayer();
            final UUID uniqueId = player.getUniqueId();
            final Location location = event.getBlock().getLocation().toCenterLocation();
            // ...
            final String name = location.x() + "_" + location.y() + "_" + location.z();
            final NamespacedKey key = new NamespacedKey("claims", "waypoint_" + (location.blockX() & 0xF) + "_" + location.blockY() + "_" + (location.blockZ() & 0xF));
            // ...
            if (waypointManager.hasWaypoint(uniqueId, name) == false) {
                event.getBlockPlaced().getChunk().getPersistentDataContainer().set(key, PersistentDataType.STRING, uniqueId.toString());
                // ...
                try {
                    waypointManager.createWaypoint(uniqueId, name, Waypoint.Source.BLOCK, location).thenAccept(isSuccess -> {
                        if (isSuccess == true) {
                            location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
                            player.setCooldown(WAYPOINT_BLOCK_TYPE, 5 * 20);
                        }
                        // TO-DO: Error message suggesting to destroy and place one more time.
                    });
                    return;
                } catch (final IllegalArgumentException ___) { /* HANDLED BELOW */ }
            }
            event.setCancelled(true);
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_ALREADY_EXISTS).placeholder("name", name).send(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointBreak(final @NotNull BlockBreakEvent event) {
        if (event.getBlock().getType() != WAYPOINT_BLOCK_TYPE)
            return;
        // ...
        final Location location = event.getBlock().getLocation().toCenterLocation();
        // ...
        final NamespacedKey key = new NamespacedKey("claims", "waypoint_" + (location.blockX() & 0xF) + "_" + location.blockY() + "_" + (location.blockZ() & 0xF));
        // ...
        if (location.getChunk().getPersistentDataContainer().has(key, PersistentDataType.STRING) == true) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            // ...
            final String owner = location.getChunk().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            // ...
            if (owner == null) {
                event.setCancelled(true); // Cancelling to prevent sync issues... (if block were removed, waypoint is inaccessible)
                throw new IllegalStateException("Chunk (" + location.getChunk().getX() + ", " + location.getChunk().getZ() + ") has key (" + key + ") set but it's value (" + owner + ") is unexpected.");
            }
            // ...
            final UUID ownerUniqueId = UUID.fromString(owner);
            // ...
            try {
                waypointManager.removeWaypoint(ownerUniqueId, location).thenAccept(isSuccess -> {
                    if (isSuccess == true) {
                        location.getChunk().getPersistentDataContainer().remove(key);
                        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                    }
                    // TO-DO: Error message suggesting to place and destroy one more time.
                });
            } catch (final IllegalArgumentException ___) { /* IGNORED */ }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY || event.getClickedBlock() == null)
            return;
        // ...
        if (event.getClickedBlock().getType() == WAYPOINT_BLOCK_TYPE) {
            event.setCancelled(true);
            new Panel(TeleportView.INVENTORY_TITLE, 54, null).open(event.getPlayer(), null).applyTemplate(new TeleportView(plugin), false);
        }
    }

}

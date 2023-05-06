package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
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

import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkDataKey;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkPosition;

@SuppressWarnings("UnstableApiUsage")
public final class WaypointListener implements Listener {

    private final Claims plugin;

    private final ClaimManager claimManager;
    private final WaypointManager waypointManager;

    public static Material WAYPOINT_BLOCK_TYPE = PluginConfig.WAYPOINT_BLOCK.getType();

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
        if (event.getBlockPlaced().getType() == WAYPOINT_BLOCK_TYPE) {
            final Player player = event.getPlayer();
            final UUID uniqueId = player.getUniqueId();
            final Location location = event.getBlock().getLocation().toCenterLocation();
            // ...
            final String name = location.x() + "_" + location.y() + "_" + location.z();
            // ...
            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // ...
            if (waypointManager.hasWaypoint(uniqueId, name) == false) {
                event.getBlockPlaced().getChunk().getPersistentDataContainer().set(key, PersistentDataType.STRING, uniqueId.toString());
                // ...
                try {
                    waypointManager.createWaypoint(uniqueId, name, Waypoint.Source.BLOCK, location).thenAccept(isSuccess -> {
                        if (isSuccess == true) /* MUST BE RUN ON MAIN THREAD */ plugin.getBedrockScheduler().run(1L, (task) -> {
                            location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
                            event.getPlayer().setCooldown(WAYPOINT_BLOCK_TYPE, 5 * 20);
                            // ...
                            final TextDisplay display = (TextDisplay) location.getWorld().spawnEntity(location.clone().add(0F, 0.75F, 0F), EntityType.TEXT_DISPLAY);
                            // Setting PDC to easily distinguish from other entities.
                            display.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                            display.text(player.name());
                            display.setBillboard(Display.Billboard.CENTER);
                            display.setShadowed(true);
                            display.setBackgroundColor(TRANSPARENT);
                            display.setViewRange(0.2F);
                        });
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
        final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
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
                waypointManager.removeWaypoint(ownerUniqueId, (waypoint) -> waypoint.getLocation().equals(location) == true).thenAccept(isSuccess -> {
                    if (isSuccess == true) /* MUST BE RUN ON MAIN THREAD */ plugin.getBedrockScheduler().run(1L, (task) -> {
                        location.getChunk().getPersistentDataContainer().remove(key);
                        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                        // ...
                        location.getNearbyEntities(2, 2, 2).stream().filter(TextDisplay.class::isInstance).forEach(entity -> {
                            if (entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE) == true)
                                entity.remove();
                        });
                    });
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
            new ClaimPanel.Builder()
                    .setClaimManager(claimManager)
                    .build()
                    .open(event.getPlayer(), (panel) -> {
                        plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                        return true;
                    });
        }
    }

}

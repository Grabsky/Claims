package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.concurrent.ExecutionException;

public final class WaypointListener implements Listener {

    private final WaypointManager waypointManager;

    public WaypointListener(final Claims plugin) {
        this.waypointManager = plugin.getClaimManager().getWaypointManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointPlace(final BlockPlaceEvent event) {
        if (event.canBuild() == false)
            return;
        // ...
        if (event.getBlockPlaced().getType() == Material.LODESTONE) {
            final Location location = event.getBlock().getLocation().add(0.5F, 1.0F, 0.5F);
            // ...
            if (waypointManager.createWaypoint(event.getPlayer().getUniqueId(), null, Waypoint.Source.BLOCK, location) == true) {
                // ...
                Message.of(PluginLocale.WAYPOINT_PLACE_SUCCESS)
                        // .placeholder("name", name)
                        .send(event.getPlayer());
            }
            // ...
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_ALREADY_EXISTS)
                    // .placeholder("name", name)
                    .send(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointBreak(final BlockBreakEvent event) {
        // ...
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaypointInteract(final PlayerInteractEvent event) throws ExecutionException {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY || event.getClickedBlock() == null)
            return;
        // ...
        if (event.getClickedBlock().getType() == Material.LODESTONE) {
            // ...
            waypointManager.getWaypoints(event.getPlayer().getUniqueId()).forEach(e -> {
                event.getPlayer().sendMessage(e.getLocation().x() + ", " + e.getLocation().y() + ", " + e.getLocation().z());
            });
        }
    }

}

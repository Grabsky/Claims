package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.waypoints.WaypointManager;
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
    public void onLodestonePlace(final BlockPlaceEvent event) {
        if (event.canBuild() == false)
            return;
        // ...
        if (event.getBlockPlaced().getType() == Material.LODESTONE) {
            waypointManager.createWaypoint(event.getPlayer().getUniqueId(), event.getBlockPlaced().getLocation());
            event.getPlayer().sendPlainMessage("placed");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLodestoneBreak(final BlockBreakEvent event) {
        // ...
    }

    @EventHandler(ignoreCancelled = true)
    public void onLoadestoneInteract(final PlayerInteractEvent event) throws ExecutionException {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY || event.getClickedBlock() == null)
            return;
        // ...
        if (event.getClickedBlock().getType() == Material.LODESTONE) {
            // ...
            waypointManager.getCache().get(event.getPlayer().getUniqueId()).forEach(e -> {
                event.getPlayer().sendMessage(e.x() + ", " + e.y() + ", " + e.z());
            });
        }
    }

}

package net.skydistrict.claims.listeners;

import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimCache;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.claims.ClaimPlayer;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

// TO-DO: Code cleanup
public class RegionListener implements Listener {
    private final ClaimManager manager;

    public RegionListener(Claims instance) {
        this.manager = instance.getClaimManager();
    }

    @EventHandler
    public void onClaimPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!event.canBuild()) return;
        if (event.getItemInHand().getType() == Material.SPONGE) {
            Player player = event.getPlayer();
            if (player.hasPermission("skydistrict.claims")) {
                UUID uuid = player.getUniqueId();
                ClaimPlayer cp = ClaimCache.getClaimPlayer(uuid);
                Location loc = event.getBlock().getLocation();
                if (!cp.hasClaim()) {
                    // The reason why it's here and not in RegionManager is that I want the messages to be different
                    if (loc.distanceSquared(Config.DEFAULT_WORLD.getSpawnLocation()) > 300) {
                        if (manager.createRegionAt(event.getBlock().getLocation(), uuid)) {
                            player.sendMessage(Lang.PLACE_SUCCESS);
                            return;
                        }
                        event.setCancelled(true);
                        player.sendMessage(Lang.OVERLAPS_OTHER_REGION);
                        return;
                    }
                    event.setCancelled(true);
                    player.sendMessage(Lang.TOO_CLOSE_TO_SPAWN);
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(Lang.REACHED_REGIONS_LIMIT);


            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClaimBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        String id = ClaimH.createId(event.getBlock().getLocation());
        if (ClaimCache.containsClaim(id)) {
            Claim claim = ClaimCache.getClaim(id);
            Player player = event.getPlayer();
            UUID ownerUniqueId = claim.getOwner();
            if (player.hasPermission("skydistrict.claims.bypass.ownercheck") || ownerUniqueId == claim.getOwner()) {
                manager.removeRegionOf(ownerUniqueId);
                player.sendMessage(Lang.DESTROY_SUCCESS);
                return;
            }
            event.setCancelled(true);
            player.sendMessage(Lang.NOT_AN_OWNER);
        }
    }

    // TO-DO: Prevent blocks from exploding and being pushed by a piston
}

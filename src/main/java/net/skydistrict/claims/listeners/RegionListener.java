package net.skydistrict.claimsgui.listeners;

import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimCache;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class RegionListener implements Listener {
    private final ClaimManager manager;

    public RegionListener(Claims instance) {
        this.manager = instance.getProvinceManager();
    }

    @EventHandler
    public void onRegionPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!event.canBuild()) return;
        if (event.getItemInHand().getType() == Material.SPONGE) {
            Player player = event.getPlayer();
            if (player.hasPermission("skydistrict.claims")) {
                Claim claim = ClaimCache.get(player.getUniqueId());
                if (claim == null) {
                    if (manager.createRegionAt(event.getBlock().getLocation(), player.getUniqueId())) {
                        player.sendMessage(Lang.PLACE_SUCCESS);
                        return;
                    }
                    player.sendMessage(Lang.OVERLAPS_OTHER_REGION);
                    return;
                }
                player.sendMessage(Lang.REACHED_REGIONS_LIMIT);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        String id = ClaimH.createId(event.getBlock().getLocation());
        if (ClaimCache.exists(id)) {
            Claim claim = ClaimCache.get(id);
            if (event.getPlayer().getUniqueId() != claim.getOwner()) return;
            // Remove the region
            manager.removeRegionWithId(id);
        }
    }

    // TO-DO: Prevent blocks from exploding and being pushed by a piston
}

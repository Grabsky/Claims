package net.skydistrict.claims.listeners;

import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.claims.ClaimPlayer;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

// TO-DO: Code cleanup
public class RegionListener implements Listener {
    private final ClaimManager manager;

    public RegionListener(Claims instance) {
        this.manager = instance.getClaimManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!event.canBuild()) return;
        PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.claimBlockLevel, PersistentDataType.INTEGER)) {
            Player player = event.getPlayer();
            if (player.hasPermission("skydistrict.claims.place")) {
                UUID uuid = player.getUniqueId();
                ClaimPlayer cp = manager.getClaimPlayer(uuid);
                Location loc = event.getBlock().getLocation();
                if (!cp.hasClaim()) {
                    // The reason why it's here and not in RegionManager is that I want the messages to be different
                    if (loc.distanceSquared(Config.DEFAULT_WORLD.getSpawnLocation()) > Config.MIN_DISTANCE_FROM_SPAWN) {
                        // This shouldn't be null
                        int level = data.get(Claims.claimBlockLevel, PersistentDataType.INTEGER);
                        if (manager.createRegionAt(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), player, level)) {
                            System.out.println("Protection black (" + level + ") has been placed down.");
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
                return;
            }
            event.setCancelled(true);
            player.sendMessage(Lang.MISSING_PERMISSIONS);
        }
    }

    // TO-DO: Do not generate location of every block placed (unless that's the best way to do so)
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        String id = ClaimH.createId(event.getBlock().getLocation());
        if (manager.containsClaim(id)) {
            Claim claim = manager.getClaim(id);
            Player player = event.getPlayer();
            UUID ownerUniqueId = claim.getOwner();
            if (player.hasPermission("skydistrict.claims.destroy")) {
                if(player.getUniqueId().equals(ownerUniqueId) || player.hasPermission("skydistrict.claims.destroy.others")) {
                    if (event.getPlayer().isSneaking()) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Deleting region
                        manager.removeRegionOf(ownerUniqueId);
                        // Dropping the item
                        if (player.getGameMode() == GameMode.SURVIVAL) event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), StaticItems.getClaimBlock(claim.getLevel()));
                        System.out.println("Protection black (" + claim.getLevel() + ") has been destroyed and returned to player.");
                        player.sendMessage(Lang.DESTROY_SUCCESS);
                        return;
                    }
                    event.setCancelled(true);
                    player.sendMessage(Lang.NOT_SNEAKING);
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(Lang.NOT_AN_OWNER);
                return;
            }
            event.setCancelled(true);
            player.sendMessage(Lang.MISSING_PERMISSIONS);
        }
    }

    // Prevents block from being pushed by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) if (manager.containsClaim(ClaimH.createId(block.getLocation()))) event.setCancelled(true);
    }

    // Prevents block from being pulled by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) if (manager.containsClaim(ClaimH.createId(block.getLocation()))) event.setCancelled(true);
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(ClaimH.createId(block.getLocation())));
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(ClaimH.createId(block.getLocation())));
    }

    // Prevents item from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (ItemStack item : event.getInventory().getMatrix()) {
            // Yes Bukkit, apparently it can be null...
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(Claims.claimBlockLevel, PersistentDataType.INTEGER)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}

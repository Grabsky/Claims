package me.grabsky.claims.listeners;

import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.configuration.Config;
import me.grabsky.claims.configuration.Items;
import me.grabsky.claims.configuration.Lang;
import me.grabsky.claims.panel.PanelManager;
import me.grabsky.claims.utils.ClaimsUtils;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Bukkit;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class RegionListener implements Listener {
    private final FileLogger fileLogger;
    private final ClaimManager manager;
    private final PanelManager panelManager;

    public RegionListener(Claims instance) {
        this.fileLogger = instance.getFileLogger();
        this.manager = instance.getClaimManager();
        this.panelManager = instance.getPanelManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!event.canBuild()) return;
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.claimBlockLevel, PersistentDataType.INTEGER)) {
            final Player player = event.getPlayer();
            if (player.hasPermission("skydistrict.plugin.claims.place")) {
                if (event.getBlock().getWorld() == Config.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer cp = manager.getClaimPlayer(uuid);
                    final Location loc = event.getBlock().getLocation();
                    if (!cp.hasClaim()) {
                        // The reason why it's here and not in RegionManager is that I want the messages to be different
                        if (!manager.isInSquare(loc, Config.DEFAULT_WORLD.getSpawnLocation(), Config.MINIMUM_DISTANCE_FROM_SPAWN)) {
                            // This shouldn't be null
                            final int level = data.get(Claims.claimBlockLevel, PersistentDataType.INTEGER);
                            final Claim claim = manager.createRegionAt(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), player, level);
                            if (claim != null) {
                                Lang.send(player, Lang.PLACE_SUCCESS);
                                // Log action if enabled
                                if (Config.LOGS) {
                                    fileLogger.log(Config.LOG_FORMAT_PLACED
                                            .replace("{claim-id}", claim.getId())
                                            .replace("{claim-level}", String.valueOf(claim.getLevel()))
                                            .replace("{issuer-name}", player.getName())
                                            .replace("{issuer-uuid}", player.getUniqueId().toString())
                                            .replace("{location}", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
                                }
                                return;
                            }
                            event.setCancelled(true);
                            Lang.send(player, Lang.OVERLAPS_OTHER_CLAIM);
                            return;
                        }
                        event.setCancelled(true);
                        Lang.send(player, Lang.TOO_CLOSE_TO_SPAWN);
                        return;
                    }
                    event.setCancelled(true);
                    Lang.send(player, Lang.REACHED_CLAIMS_LIMIT);
                    return;
                }
                event.setCancelled(true);
                Lang.send(player, Lang.BLACKLISTED_WORLD);
                return;
            }
            event.setCancelled(true);
            Lang.send(player, Global.MISSING_PERMISSIONS);
        }
    }

    // TO-DO: Do not generate location of every block placed (unless that's the best way to do so)
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld() != Config.DEFAULT_WORLD) return;
        final String id = ClaimsUtils.createId(event.getBlock().getLocation());
        if (manager.containsClaim(id)) {
            final Claim claim = manager.getClaim(id);
            final Player player = event.getPlayer();
            final UUID ownerUniqueId = claim.getOwner();
            if (player.hasPermission("skydistrict.plugin.claims.destroy")) {
                if (player.getUniqueId().equals(ownerUniqueId) || player.hasPermission("skydistrict.bypass.claims.ownercheck")) {
                    if (event.getPlayer().isSneaking()) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Log action if enabled
                        if (Config.LOGS) {
                            fileLogger.log(Config.LOG_FORMAT_DESTROYED
                                    .replace("{claim-id}", id)
                                    .replace("{claim-level}", String.valueOf(claim.getLevel()))
                                    .replace("{owner-name}", UserCache.get(ownerUniqueId).getName())
                                    .replace("{owner-uuid}", ownerUniqueId.toString())
                                    .replace("{issuer-name}", player.getName())
                                    .replace("{issuer-uuid}", player.getUniqueId().toString()));
                        }
                        // Deleting region
                        manager.removeRegionOf(ownerUniqueId);
                        // Dropping the item
                        if (player.getGameMode() == GameMode.SURVIVAL) event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), Items.getClaimBlock(claim.getLevel()));
                        Lang.send(player, Lang.DESTROY_SUCCESS);
                        final Player owner = Bukkit.getPlayer(ownerUniqueId);
                        if (owner != null && owner.isOnline()) {
                            if (panelManager.isInventoryOpen(owner)) {
                                owner.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            }
                        }
                        return;
                    }
                    event.setCancelled(true);
                    Lang.send(player, Lang.NOT_SNEAKING);
                    return;
                }
                event.setCancelled(true);
                Lang.send(player, Lang.NOT_OWNER);
                return;
            }
            event.setCancelled(true);
            Lang.send(player, Global.MISSING_PERMISSIONS);
        }
    }

    // Prevents block from being pushed by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) if (manager.containsClaim(ClaimsUtils.createId(block.getLocation()))) event.setCancelled(true);
    }

    // Prevents block from being pulled by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) if (manager.containsClaim(ClaimsUtils.createId(block.getLocation()))) event.setCancelled(true);
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(ClaimsUtils.createId(block.getLocation())));
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(ClaimsUtils.createId(block.getLocation())));
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

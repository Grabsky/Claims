package me.grabsky.claims.listeners;

import me.grabsky.claims.Claims;
import me.grabsky.claims.ClaimsKeys;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimLevel;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Items;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.UserCache;
import me.grabsky.indigo.utils.Numbers;
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

    public RegionListener(Claims instance) {
        this.fileLogger = instance.getFileLogger();
        this.manager = instance.getClaimManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!event.canBuild()) return; // Not sure if that check covers all possible scenarios including building in WorldGuard regions
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(ClaimsKeys.CLAIM_LEVEL, PersistentDataType.INTEGER)) {
            final Player player = event.getPlayer();
            if (player.hasPermission("claims.plugin.place")) {
                if (event.getBlock().getWorld() == ClaimsConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer cp = manager.getClaimPlayer(uuid);
                    final Location loc = event.getBlock().getLocation();
                    if (!cp.hasClaim()) {
                        // The reason why it's here and not in RegionManager is that I want the messages to be different
                        if (!manager.isInSquare(loc, ClaimsConfig.DEFAULT_WORLD.getSpawnLocation(), ClaimsConfig.MINIMUM_DISTANCE_FROM_SPAWN)) {
                            // This shouldn't be null
                            final int level = data.get(ClaimsKeys.CLAIM_LEVEL, PersistentDataType.INTEGER);
                            final Claim claim = manager.createRegionAt(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), player, level);
                            if (claim != null) {
                                ClaimsLang.send(player, ClaimsLang.PLACE_SUCCESS);
                                // Log action if enabled
                                if (ClaimsConfig.LOGS) {
                                    fileLogger.log(ClaimsConfig.LOG_FORMAT_PLACED
                                            .replace("{claim-id}", claim.getId())
                                            .replace("{claim-level}", String.valueOf(claim.getLevel()))
                                            .replace("{issuer-name}", player.getName())
                                            .replace("{issuer-uuid}", player.getUniqueId().toString())
                                            .replace("{location}", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
                                }
                                return;
                            }
                            event.setCancelled(true);
                            ClaimsLang.send(player, ClaimsLang.OVERLAPS_OTHER_CLAIM);
                            return;
                        }
                        event.setCancelled(true);
                        ClaimsLang.send(player, ClaimsLang.TOO_CLOSE_TO_SPAWN);
                        return;
                    }
                    event.setCancelled(true);
                    ClaimsLang.send(player, ClaimsLang.REACHED_CLAIMS_LIMIT);
                    return;
                }
                event.setCancelled(true);
                ClaimsLang.send(player, ClaimsLang.BLACKLISTED_WORLD);
                return;
            }
            event.setCancelled(true);
            ClaimsLang.send(player, Global.MISSING_PERMISSIONS);
        }
    }

    // TO-DO: Do not generate location of every block placed (unless that's the best way to do so)
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld() != ClaimsConfig.DEFAULT_WORLD) return;
        final String id = Claim.createId(event.getBlock().getLocation());
        if (manager.containsClaim(id)) {
            final Claim claim = manager.getClaim(id);
            final Player player = event.getPlayer();
            final UUID ownerUniqueId = claim.getOwner();
            if (player.hasPermission("claims.plugin.destroy")) {
                if (player.getUniqueId().equals(ownerUniqueId) || player.hasPermission("claims.bypass.ownercheck")) {
                    if (event.getPlayer().isSneaking()) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Logging action if enabled
                        if (ClaimsConfig.LOGS) {
                            fileLogger.log(ClaimsConfig.LOG_FORMAT_DESTROYED
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
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), ClaimLevel.getClaimLevel(claim.getLevel()).getClaimBlockItem());
                        }
                        ClaimsLang.send(player, ClaimsLang.DESTROY_SUCCESS);
                        final Player owner = Bukkit.getPlayer(ownerUniqueId);
                        if (owner != null && owner.isOnline()) {
                            if (owner.getOpenInventory().getTopInventory().getHolder() instanceof Panel) {
                                owner.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            }
                        }
                        return;
                    }
                    event.setCancelled(true);
                    ClaimsLang.send(player, ClaimsLang.NOT_SNEAKING);
                    return;
                }
                event.setCancelled(true);
                ClaimsLang.send(player, ClaimsLang.NOT_OWNER);
                return;
            }
            event.setCancelled(true);
            ClaimsLang.send(player, Global.MISSING_PERMISSIONS);
        }
    }

    @EventHandler
    public void onAmethystBreak(BlockBreakEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getBlock().getType() == Material.AMETHYST_CLUSTER) {
            if (Numbers.chanceOf(0.02F)) {
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), Items.UPGRADE_CRYSTAL);
            }
        }
    }

    // Prevents block from being pushed by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (manager.containsClaim(Claim.createId(block.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being pulled by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (manager.containsClaim(Claim.createId(block.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(Claim.createId(block.getLocation())));
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(Claim.createId(block.getLocation())));
    }

    // Prevents item from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (ItemStack item : event.getInventory().getMatrix()) {
            // Yes Bukkit, apparently it can be null...
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(ClaimsKeys.CLAIM_LEVEL, PersistentDataType.INTEGER)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}

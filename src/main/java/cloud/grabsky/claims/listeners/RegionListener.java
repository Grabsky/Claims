package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
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

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;

public class RegionListener implements Listener {
    private final ClaimManager manager;

    public RegionListener(final Claims instance) {
        this.manager = instance.getClaimManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPlace(final BlockPlaceEvent event) {
        // Not sure if that check covers all possible scenarios including building in WorldGuard regions
        if (event.isCancelled() || event.canBuild() == false)
            return;
        // Checking if placed block is a claim block
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.Key.CLAIM_LEVEL, PersistentDataType.STRING) == true) {
            final Player player = event.getPlayer();
            // Checking if player has permission to create a claim
            if (player.hasPermission("claims.plugin.place") == true) {
                // Checking if player can create claim in that world
                if (event.getBlock().getWorld() == PluginConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer claimPlayer = manager.getClaimPlayer(uuid);
                    final Location loc = event.getBlock().getLocation();
                    // Making sure player DOES NOT have a claim
                    if (claimPlayer.hasClaim() == false) {
                        // Making sure that placed region is further enough from spawn
                        if (!manager.isInSquare(loc, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN)) {
                            final String level = data.get(Claims.Key.CLAIM_LEVEL, PersistentDataType.STRING); // This shouldn't be null
                            // Finally, trying to create a claim
                            final Claim claim = manager.createRegionAt(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), player, level);
                            if (claim != null) {
                                sendMessage(player, PluginLocale.PLACE_SUCCESS);
                                return;
                            }
                            event.setCancelled(true);
                            sendMessage(player, PluginLocale.OVERLAPS_OTHER_CLAIM);
                            return;
                        }
                        event.setCancelled(true);
                        sendMessage(player, PluginLocale.TOO_CLOSE_TO_SPAWN);
                        return;
                    }
                    event.setCancelled(true);
                    sendMessage(player, PluginLocale.REACHED_CLAIMS_LIMIT);
                    return;
                }
                event.setCancelled(true);
                sendMessage(player, PluginLocale.BLACKLISTED_WORLD);
                return;
            }
            event.setCancelled(true);
            sendMessage(player, "No permissions.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBreak(final BlockBreakEvent event) {
        // Preconditions
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld() != PluginConfig.DEFAULT_WORLD) return;
        // Checking if destroyed block is a claim block
        final String id = Claim.createId(event.getBlock().getLocation());
        if (manager.containsClaim(id) == true) {
            final Claim claim = manager.getClaim(id);
            final Player player = event.getPlayer();
            final UUID ownerUniqueId = claim.getOwner().getUniqueId();
            // Checking if player has permission to destroy a claim
            if (player.hasPermission("claims.plugin.destroy")) {
                // Checking if player CAN destroy the claim
                if (player.getUniqueId().equals(ownerUniqueId) || player.hasPermission("claims.bypass.ownercheck")) {
                    // Checking if player is sneaking
                    if (event.getPlayer().isSneaking()) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Deleting region
                        manager.removeRegionOf(ownerUniqueId);
                        // Dropping the item
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), claim.getType().getBlock());
                        }
                        sendMessage(player, PluginLocale.DESTROY_SUCCESS);
                        // Closing owner's claim management inventory (if open)
                        final Player owner = Bukkit.getPlayer(ownerUniqueId);
                        if (owner != null && owner.isOnline() == true) {
                            if (owner.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel) {
                                owner.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            }
                        }
                        return;
                    }
                    event.setCancelled(true);
                    sendMessage(player, PluginLocale.NOT_SNEAKING);
                    return;
                }
                event.setCancelled(true);
                sendMessage(player, PluginLocale.NOT_OWNER);
                return;
            }
            event.setCancelled(true);
            sendMessage(player, "No permissions.");
        }
    }

    // Prevents block from being pushed by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        for (final Block block : event.getBlocks()) {
            if (manager.containsClaim(Claim.createId(block.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being pulled by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        for (final Block block : event.getBlocks()) {
            if (manager.containsClaim(Claim.createId(block.getLocation())) == true) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(Claim.createId(block.getLocation())) == true);
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(block -> manager.containsClaim(Claim.createId(block.getLocation())) == true);
    }

    // Prevents item from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftPrepare(final PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (final ItemStack item : event.getInventory().getMatrix()) {
            // Yes Bukkit, apparently it can be null...
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER) == true) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}

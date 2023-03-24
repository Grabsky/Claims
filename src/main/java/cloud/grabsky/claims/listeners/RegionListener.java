package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

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
        if (event.isCancelled() == true || event.canBuild() == false)
            return;
        // Checking if placed block is a claim block
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING) == true) {
            final Player player = event.getPlayer();
            // Checking if player has permission to create a claim
            if (player.hasPermission("claims.plugin.place") == true) {
                // Checking if player can create claim in that world
                if (event.getBlock().getWorld() == PluginConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer claimPlayer = manager.getClaimPlayer(uuid);
                    final Location loc = event.getBlock().getLocation();
                    // Making sure player DOES NOT have a claim
                    if (player.hasPermission("claims.bypass.claim_limit") == true || claimPlayer.getClaims().size() <= 5) {
                        // Making sure that placed region is further enough from spawn
                        if (manager.isInSquare(loc, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN) == false) {
                            final String type = data.get(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING); // This shouldn't be null
                            // Checking if player has all existing claims fully upgraded
                            if (player.hasPermission("claims.bypass.claim_limit") == true || manager.getClaimTypes().get(type).getNextType() == null || claimPlayer.getClaims().stream().anyMatch(claim -> claim.getType().isUpgradeable() == true) == false) {
                                // Finally, trying to create a claim
                                final Claim claim = manager.createClaim(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), player, type);
                                if (claim != null) {
                                    sendMessage(player, PluginLocale.PLACEMENT_PLACE_SUCCESS);
                                    return;
                                }
                                event.setCancelled(true);
                                sendMessage(player, PluginLocale.PLACEMENT_PLACE_FAILURE_OVERLAPS);
                                return;
                            }
                            event.setCancelled(true);
                            sendMessage(player, PluginLocale.PLACEMENT_PLACE_FAILURE_OTHER_CLAIMS_MUST_BE_UPGRADED);
                            return;
                        }
                        event.setCancelled(true);
                        sendMessage(player, PluginLocale.PLACEMENT_PLACE_FAILURE_TOO_CLOSE_TO_SPAWN);
                        return;
                    }
                    event.setCancelled(true);
                    sendMessage(player, PluginLocale.PLACEMENT_PLACE_FAILURE_REACHED_CLAIMS_LIMIT);
                    return;
                }
                event.setCancelled(true);
                sendMessage(player, PluginLocale.PLACEMENT_PLACE_FAILURE_BLACKLISTED_WORLD);
                return;
            }
            event.setCancelled(true);
            sendMessage(player, PluginLocale.MISSING_PERMISSIONS);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimBreak(final BlockBreakEvent event) {
        // Preconditions
        if (event.isCancelled() == true || event.getBlock().getWorld() != PluginConfig.DEFAULT_WORLD)
            return;
        // Checking if destroyed block is a claim block
        final String id = Claim.createId(event.getBlock().getLocation());
        // ...
        if (manager.containsClaim(id) == true) {
            final Claim claim = manager.getClaim(id);
            final Player player = event.getPlayer();
            final ClaimPlayer claimPlayer = manager.getClaimPlayer(player);
            // Checking if player has permission to destroy a claim
            if (player.hasPermission("claims.plugin.destroy") == true) {
                // Checking if player CAN destroy the claim
                if (claimPlayer.isOwnerOf(claim) == true || player.hasPermission("claims.bypass.ownercheck") == true) {
                    // Checking if player is sneaking
                    if (event.getPlayer().isSneaking() == true) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Closing owner's claim management inventory (if open)
                        claim.getOwners().forEach(owner -> {
                           final @Nullable Player pOwner = owner.toPlayer();
                           // ...
                           if (pOwner != null && pOwner.isOnline() == true)
                               if (pOwner.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel)
                                   pOwner.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

                        });
                        // Deleting region
                        manager.deleteClaim(claim);
                        // Dropping the item
                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), claim.getType().getBlock());
                        }
                        // ...
                        sendMessage(player, PluginLocale.PLACEMENT_DESTROY_SUCCESS);
                        return;
                    }
                    event.setCancelled(true);
                    sendMessage(player, PluginLocale.PLACEMENT_DESTROY_FAILURE_NOT_SNEAKING);
                    return;
                }
                event.setCancelled(true);
                sendMessage(player, PluginLocale.NOT_CLAIM_OWNER);
                return;
            }
            event.setCancelled(true);
            sendMessage(player, PluginLocale.MISSING_PERMISSIONS);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY || event.getClickedBlock() == null)
            return;
        // ...
        final String id = Claim.createId(event.getClickedBlock().getLocation());
        // ...
        if (manager.containsClaim(id) == true) {
            System.out.println(id);
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
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(Claims.Key.CLAIM_TYPE, PersistentDataType.INTEGER) == true) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }
}

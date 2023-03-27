package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.views.ViewMain;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanel;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class RegionListener implements Listener {

    private final Claims claims;
    private final ClaimManager claimManager;

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPlace(final BlockPlaceEvent event) {
        // Not sure if that covers all possible cases including building in WorldGuard regions, but it's better than nothing.
        if (event.isCancelled() == true || event.canBuild() == false)
            return;
        // Checking if placed block is a claim block.
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING) == true) {
            final Player player = event.getPlayer();
            // Checking if player has permission to create a claim.
            if (player.hasPermission("claims.plugin.place") == true) {
                // Checking if player can create claim in that world
                if (event.getBlock().getWorld() == PluginConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(uuid);
                    final Location location = event.getBlock().getLocation(); // This is already a copy, meaning it can be freely modified.
                    // Making sure player does not exceed claim limit.
                    if (player.hasPermission("claims.bypass.claim_limit") == true || claimPlayer.getClaims().size() < PluginConfig.CLAIMS_LIMIT) {
                        // Making sure that placed region is further enough from spawn
                        if (claimManager.isWithinSquare(location, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN) == false) {
                            final String type = data.get(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING); // This shouldn't be null
                            // Checking if player has all existing claims fully upgraded.
                            if (player.hasPermission("claims.bypass.claim_limit") == true || claimManager.getClaimTypes().get(type).getNextType() == null || claimPlayer.getClaims().stream().anyMatch(claim -> claim.getType().isUpgradeable() == true) == false) {
                                // Finally, trying to create a claim. If it returns null, it means it's colliding with other region.
                                final Claim claim = claimManager.createClaim(location.add(0.5, 0.5, 0.5), player, type);
                                // ...
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
        if (claimManager.containsClaim(id) == true) {
            final Claim claim = claimManager.getClaim(id);
            final Player player = event.getPlayer();
            final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(player);
            // Checking if player has permission to destroy a claim
            if (player.hasPermission("claims.plugin.destroy") == true) {
                // Checking if player CAN destroy the claim
                if (claimPlayer.isOwnerOf(claim) == true || player.hasPermission("claims.bypass.ownercheck") == true) {
                    // Checking if player is sneaking
                    if (event.getPlayer().isSneaking() == true) {
                        // Removing drops
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Closing owners' claim management inventory (if open)
                        claim.getOwners().stream().map(ClaimPlayer::toPlayer).filter(Objects::nonNull).filter(Player::isOnline).forEach(owner -> {
                            if (isClaimPanel(owner.getOpenInventory()) == true)
                                owner.closeInventory();
                        });
                        // Deleting the Claim (and region)
                        claimManager.deleteClaim(claim);
                        // Dropping claim block item if player is not creative.
                        if (player.getGameMode() == GameMode.SURVIVAL)
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), claim.getType().getBlock());
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

    // TO-DO: Make sure none else has the panel open.
    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY || event.getClickedBlock() == null)
            return;
        // ...
        final String id = Claim.createId(event.getClickedBlock().getLocation());
        // ...
        if (claimManager.containsClaim(id) == true) {
            final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(event.getPlayer());
            final Claim claim = claimManager.getClaim(id);
            // ...
            if (claim != null && (event.getPlayer().hasPermission("claims.command.edit") == true|| claim.isOwner(claimPlayer) == true) == true) {
                // ...
                new ClaimPanel(claimManager, claim).open(event.getPlayer(), (panel) -> {
                    claims.getBedrockScheduler().run(1L, (task) -> panel.applyTemplate(new ViewMain(), false));
                    return true;
                });
            }
        }
    }

    // Prevents block from being pushed by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        for (final Block block : event.getBlocks()) {
            if (claimManager.containsClaim(Claim.createId(block.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being pulled by piston
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        for (final Block block : event.getBlocks()) {
            if (claimManager.containsClaim(Claim.createId(block.getLocation())) == true) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        event.blockList().removeIf(block -> claimManager.containsClaim(Claim.createId(block.getLocation())) == true);
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(block -> claimManager.containsClaim(Claim.createId(block.getLocation())) == true);
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

package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseCategories;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanelOpen;
import static cloud.grabsky.claims.util.Utilities.getAroundPosition;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class RegionListener implements Listener {

    private final Claims claims;
    private final ClaimManager claimManager;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // TO-DO: Some sort of cooldown? Perhaps allow collision between owned regions?
    public void onClaimPlace(final BlockPlaceEvent event) {
        // Not sure if that covers all possible cases including building in WorldGuard regions, but it's better than nothing.
        if (event.canBuild() == false)
            return;
        // Checking if placed block is a claim block.
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING) == true) {
            final Player player = event.getPlayer();
            // Setting 5 second cooldown to prevent block place spam. Unfortunately this works per-material and not per-itemstack.
            event.getPlayer().setCooldown(event.getItemInHand().getType(), PluginConfig.PLACE_ATTEMPT_COOLDOWN * 20);
            // Checking if player has permission to create a claim.
            if (player.hasPermission("claims.plugin.place") == true) {
                // Checking if player can create claim in that world
                if (event.getBlock().getWorld() == PluginConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(uuid);
                    final Location location = event.getBlock().getLocation(); // This is already a copy, meaning it can be freely modified.
                    // Making sure player does not exceed claim limit.
                    if (player.hasPermission("claims.bypass.ignore_claims_limit") == true || claimPlayer.getClaims().size() < PluginConfig.CLAIMS_LIMIT) {
                        // Making sure that placed region is far enough from spawn
                        if (ClaimManager.isWithinSquare(location, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.MINIMUM_DISTANCE_FROM_SPAWN) == false) {
                            final Claim.Type type = claimManager.getClaimTypes().get(data.get(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING));
                            // ...
                            if (type != null) {
                                // Checking if player has all existing claims fully upgraded.
                                if (player.hasPermission("claims.bypass.ignore_claims_limit") == true || type.isUpgradeable() == false || claimPlayer.getClaims().stream().anyMatch(claim -> claim.getType().isUpgradeable() == true) == false) {
                                    // Finally, trying to create a claim.
                                    if (claimManager.createClaim(location.add(0.5, 0.5, 0.5), player, type) == true) {
                                        Message.of(PluginLocale.PLACEMENT_PLACE_SUCCESS).send(player);
                                        return;
                                    }
                                    event.setCancelled(true);
                                    Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_OVERLAPS).send(player);
                                    return;
                                }
                                event.setCancelled(true);
                                Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_OTHER_CLAIMS_MUST_BE_UPGRADED).send(player);
                                return;
                            }
                            event.setCancelled(true);
                            Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_INVALID_CLAIM_TYPE).send(player);
                            return;
                        }
                        event.setCancelled(true);
                        Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_TOO_CLOSE_TO_SPAWN).send(player);
                        return;
                    }
                    event.setCancelled(true);
                    Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_REACHED_CLAIMS_LIMIT).send(player);
                    return;
                }
                event.setCancelled(true);
                Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_BLACKLISTED_WORLD).send(player);
                return;
            }
            event.setCancelled(true);
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClaimBreak(final BlockBreakEvent event) {
        // Skipping event for disabled worlds.
        if (event.getBlock().getWorld() != PluginConfig.DEFAULT_WORLD)
            return;
        // Generating claim ID at block break location.
        final String id = Claim.createId(event.getBlock().getLocation());
        // Checking if claim with that ID exists.
        if (claimManager.containsClaim(id) == true) {
            final @Nullable Claim claim = claimManager.getClaim(id); // Marked as @Nullable but should not be null.
            final Player player = event.getPlayer();
            final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(player);
            // Checking if player has permission to destroy a claim.
            if (player.hasPermission("claims.plugin.destroy") == true) {
                // Checking if player can destroy this specific claim.
                if (player.hasPermission("claims.plugin.can_modify_unowned_claim") == true || claimPlayer.isOwnerOf(claim) == true) {
                    // Checking if player is sneaking.
                    if (event.getPlayer().isSneaking() == true) {
                        // Removing drops. Item is dropped independently.
                        event.setExpToDrop(0);
                        event.setDropItems(false);
                        // Closing claim management interface in case anyone has it open.
                        Bukkit.getOnlinePlayers().stream().map(Player::getOpenInventory)
                                .filter((it) -> (it.getTopInventory().getHolder() instanceof ClaimPanel cPanel && cPanel.getClaim().equals(claim) == true))
                                .forEach(InventoryView::close);
                        // Deleting the claim and associated region.
                        claimManager.deleteClaim(claim);
                        // Dropping claim block item if player is not in creative mode.
                        if (player.getGameMode() == GameMode.SURVIVAL)
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), claim.getType().getBlock());
                        // Showing success message.
                        Message.of(PluginLocale.PLACEMENT_DESTROY_SUCCESS).send(player);
                        return;
                    }
                    event.setCancelled(true);
                    Message.of(PluginLocale.PLACEMENT_DESTROY_FAILURE_NOT_SNEAKING).send(player);
                    return;
                }
                event.setCancelled(true);
                Message.of(PluginLocale.NOT_CLAIM_OWNER).send(player);
                return;
            }
            event.setCancelled(true);
            Message.of(PluginLocale.MISSING_PERMISSIONS).send(player);
        }
    }

    // TO-DO: Make sure none else has the claim panel open.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClaimInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.getItem() != null || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY)
            return;
        // Following check is (most likely) redundant because of the action check.
        if (event.getClickedBlock() == null)
            return;
        // ...
        final String id = Claim.createId(event.getClickedBlock().getLocation());
        // ...
        if (claimManager.containsClaim(id) == true) {
            event.setCancelled(true);
            // ...
            final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(event.getPlayer());
            final Claim claim = claimManager.getClaim(id);
            // ...
            if (claim != null) {
                // Opening FULL panel
                if (event.getPlayer().hasPermission("claims.plugin.can_modify_unowned_claims") == true || claim.isOwner(claimPlayer) == true) {
                    // Cancelling in case panel is already in use.
                    if (isClaimPanelOpen(claim) == true) {
                        Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_FAILURE_ALREADY_IN_USE).send(event.getPlayer());
                        return;
                    }
                    // Otherwise, creating and opening the panel.
                    new ClaimPanel.Builder().setClaimManager(claimManager).setClaim(claim).build()
                            .open(event.getPlayer(), (panel) -> {
                                if (panel instanceof ClaimPanel cPanel) {
                                    claims.getBedrockScheduler().run(1L, (task) -> cPanel.applyTemplate(BrowseCategories.INSTANCE, false));
                                    return true;
                                }
                                return false;
                            });
                } else if (claim.isMember(claimPlayer) == true) {
                    new ClaimPanel.Builder().setClaimManager(claimManager).build()
                            .open(event.getPlayer(), (panel) -> {
                                if (panel instanceof ClaimPanel cPanel) {
                                    claims.getBedrockScheduler().run(1L, (task) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                                    return true;
                                }
                                return false;
                            });
                }
            }
        }
    }

    // Prevents block from being pushed by a piston.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        for (final Block block : event.getBlocks()) {
            if (claimManager.containsClaim(Claim.createId(block.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being pulled by a piston.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        for (final Block block : event.getBlocks()) {
            if (claimManager.containsClaim(Claim.createId(block.getLocation())) == true) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents block from being destroyed because of block explosion (TNT)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        event.blockList().removeIf(block -> claimManager.containsClaim(Claim.createId(block.getLocation())) == true);
    }

    // Prevents block from being destroyed because of entity explosion
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(block -> claimManager.containsClaim(Claim.createId(block.getLocation())) == true);
    }

    // Prevents item from being a crafting ingredient
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftPrepare(final PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        for (final ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && containsClaimType(item) == true) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    // Prevents item from being used as a furnace fuel.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceBurn(final FurnaceBurnEvent event) {
        if (containsClaimType(event.getFuel()) == true)
            event.setCancelled(true);
    }

    // Prevents item from being renamed/combined in anvil.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilPrepare(final PrepareAnvilEvent event) {
        if (event.getInventory().getFirstItem() != null && containsClaimType(event.getInventory().getFirstItem()) == true)
            event.setResult(null);
        if (event.getInventory().getSecondItem() != null && containsClaimType(event.getInventory().getSecondItem()) == true)
            event.setResult(null);
    }

    // Prevents item from being used in stonecutter recipe.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStonecutterRecipeSelect(final PlayerStonecutterRecipeSelectEvent event) {
        if (event.getStonecutterInventory().getInputItem() != null && containsClaimType(event.getStonecutterInventory().getInputItem()) == true)
            event.getStonecutterInventory().setResult(null);
    }

    // Prevents item from being used in smithing recipe.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSmithingPrepare(final PrepareSmithingEvent event) {
        if (event.getInventory().getInputEquipment() != null && containsClaimType(event.getInventory().getInputEquipment()) == true)
            event.setResult(null);
        if (event.getInventory().getInputMineral() != null && containsClaimType(event.getInventory().getInputMineral()) == true)
            event.setResult(null);
    }

    // Preventing block from being used to summon entity(-ies) like Iron Golem, Snow Golem or Wither.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSummonEntity(final EntitySpawnEvent event) {
        switch (event.getEntity().getEntitySpawnReason()) {
            case BUILD_IRONGOLEM, BUILD_SNOWMAN, BUILD_WITHER -> {
                // ................................. This checks 3x3x3 area around the entity. "Normal" cases should be fully covered.
                final boolean isAnyClaimSuperClose = getAroundPosition(event.getLocation().toBlock(), 1).anyMatch((pos) -> {
                    // Generating the claim identifier from given position.
                    final String id = Claim.createId(pos);
                    // ...
                    return claimManager.containsClaim(id);
                });
                // ...
                if (isAnyClaimSuperClose == true) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private static boolean containsClaimType(final @NotNull ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING) == true;
    }

}

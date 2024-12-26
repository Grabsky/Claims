/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.listeners;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseCategories;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.session.Session;
import com.destroystokyo.paper.MaterialTags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Crafter;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanelOpen;
import static cloud.grabsky.claims.util.Utilities.getAroundPosition;
import static org.bukkit.persistence.PersistentDataType.STRING;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class RegionListener implements Listener {

    private final Claims claims;
    private final ClaimManager claimManager;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // TO-DO: Perhaps allow for collisions between owned regions?
    public void onClaimPlace(final BlockPlaceEvent event) {
        // Not sure if that covers all possible cases including building in WorldGuard regions, but it's better than nothing.
        if (event.canBuild() == false || event.getItemInHand().hasItemMeta() == false)
            return;
        // Checking if placed block is a claim block.
        final PersistentDataContainer data = event.getItemInHand().getItemMeta().getPersistentDataContainer();
        if (data.has(Claims.Key.CLAIM_TYPE, STRING) == true) {
            final Player player = event.getPlayer();
            // Checking if player has permission to create a claim.
            if (player.hasPermission("claims.plugin.place") == true) {
                // Checking if player can create claim in that world
                if (event.getBlock().getWorld() == PluginConfig.DEFAULT_WORLD) {
                    final UUID uuid = player.getUniqueId();
                    final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(uuid);
                    final Location location = event.getBlock().getLocation(); // This is already a copy, meaning it can be freely modified.
                    // Making sure player does not exceed claim limit.
                    if (player.hasPermission("claims.bypass.ignore_claims_limit") == true || claimPlayer.getClaims().size() < claimPlayer.getClaimsLimit()) {
                        // Setting 5 second cooldown to prevent block place spam. Unfortunately this works per-material and not per-item.
                        event.getPlayer().setCooldown(event.getItemInHand().getType(), PluginConfig.CLAIM_SETTINGS_PLACE_ATTEMPT_COOLDOWN * 20);
                        // Making sure that placed region is far enough from spawn
                        if (ClaimManager.isWithinSquare(location, PluginConfig.DEFAULT_WORLD.getSpawnLocation(), PluginConfig.CLAIMS_SETTINGS_MINIMUM_DISTANCE_FROM_SPAWN) == false) {
                            final Claim.Type type = claimManager.getClaimTypes().get(data.get(Claims.Key.CLAIM_TYPE, STRING));
                            // ...
                            if (type != null) {
                                // Checking if player has all existing claims fully upgraded.
                                if (player.hasPermission("claims.bypass.ignore_claims_limit") == true || type.isUpgradeable() == false || claimPlayer.getClaims().stream().anyMatch(claim -> claim.getType().isUpgradeable() == true) == false) {
                                    // Finally, trying to create a claim.
                                    final @Nullable Claim claim = claimManager.createClaim(location.add(0.5, 0.5, 0.5), player, type);
                                    // Post creation actions...
                                    if (claim != null) {
                                        // Sending success message.
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
                        Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_TOO_CLOSE_TO_SPAWN).placeholder("distance", PluginConfig.CLAIMS_SETTINGS_MINIMUM_DISTANCE_FROM_SPAWN).send(player);
                        return;
                    }
                    event.setCancelled(true);
                    Message.of(PluginLocale.PLACEMENT_PLACE_FAILURE_REACHED_CLAIMS_LIMIT).placeholder("limit", claimPlayer.getClaimsLimit()).send(player);
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
                        // ...
                        final Location location = event.getBlock().getLocation();
                        // Invalidating sessions and closing inventories.
                        Bukkit.getOnlinePlayers().forEach((onlinePlayer) -> {
                            final UUID onlineUniqueId = onlinePlayer.getUniqueId();
                            final @Nullable Session<?> session = Session.Listener.CURRENT_EDIT_SESSIONS.getIfPresent(onlineUniqueId);
                            if (session != null) {
                                final @Nullable Location sessionAccessBlockLocation = session.getAssociatedPanel().getAccessBlockLocation();
                                // Skipping unrelated sessions.
                                if (sessionAccessBlockLocation != null && (location.equals(sessionAccessBlockLocation) == true || session.getSubject().equals(claim) == true)) {
                                    final @Nullable Player sessionOperator = Bukkit.getPlayer(onlineUniqueId);
                                    // Invalidating and clearing the title.
                                    if (sessionOperator != null && sessionOperator.isOnline() == true) {
                                        Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(onlineUniqueId);
                                        sessionOperator.clearTitle();
                                    }
                                }
                            }
                            // Closing open panels.
                            if (onlinePlayer.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel cPanel)
                                if (cPanel.getClaim() != null && cPanel.getClaim().equals(claim) == true)
                                    onlinePlayer.closeInventory();
                        });
                        // Deleting the claim and associated region.
                        claimManager.deleteClaim(claim);
                        // Dropping claim block item if player is not in creative mode.
                        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                            // Getting identifier of this claim type.
                            final String typeId = claim.getType().getId();
                            // Getting block item of this claim type.
                            final ItemStack blockItem = claim.getType().getBlock();
                            // Dropping claim block item.
                            event.getBlock().getWorld().dropItem(
                                    event.getBlock().getLocation(),
                                    new ItemBuilder(blockItem).setPersistentData(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING, typeId).build()
                            );
                        }
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
        // Skipping non-right-click-block actions and cancelled actions.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY)
            return;
        // Getting the clicked block.
        final @Nullable Block block = event.getClickedBlock();
        // Following check is probably redundant because of the action check. We still keep it here to get rid of nullability warning.
        if (block == null)
            return;
        // Generating ID for given block location.
        final String id = Claim.createId(block.getLocation());
        // Checking if generated ID is a registered claim.
        if (claimManager.containsClaim(id) == true) {
            // Skipping when player is trying to place a block.
            if (event.getPlayer().isSneaking() == true && (isBlockOrSummonsEntity(event.getPlayer().getInventory().getItemInMainHand()) == true || isBlockOrSummonsEntity(event.getPlayer().getInventory().getItemInMainHand())) == true)
                return;
            // ...
            event.setCancelled(true);
            // Make sure to handle interaction only for the main hand.
            if (event.getHand() != EquipmentSlot.HAND)
                return;
            // ...
            final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(event.getPlayer());
            final Claim claim = claimManager.getClaim(id);
            // ...
            if (claim != null) {
                // Opening FULL panel
                if (event.getPlayer().hasPermission("claims.plugin.can_modify_unowned_claims") == true || claim.isOwner(claimPlayer) == true) {
                    // Cancelling in case panel is already in use.
                    if (isClaimPanelOpen(claim) == true) {
                        Message.of(PluginLocale.CLAIMS_ALREADY_EDITING).send(event.getPlayer());
                        return;
                    }
                    // Otherwise, creating and opening the panel.
                    new ClaimPanel.Builder()
                            .setClaimManager(claimManager)
                            .setClaim(claim)
                            .setAccessBlockLocation(block.getLocation())
                            .build().open(event.getPlayer(), (panel) -> {
                                if (panel instanceof ClaimPanel cPanel) {
                                    claims.getBedrockScheduler().run(1L, (task) -> cPanel.applyClaimTemplate(BrowseCategories.INSTANCE, false));
                                    return true;
                                }
                                return false;
                            });
                } else if (claimPlayer.isMemberOf(claim) == true)
                    new ClaimPanel.Builder()
                        .setClaimManager(claimManager)
                        .setAccessBlockLocation(block.getLocation())
                        .build().open(event.getPlayer(), (panel) -> {
                            if (panel instanceof ClaimPanel cPanel) {
                                claims.getBedrockScheduler().run(1L, (task) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                                return true;
                            }
                            return false;
                        });
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

    // Prevents item from being a crafting ingredient in crafting table.
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

    // Prevents item from being a crafting ingredient in crafter.
    @SuppressWarnings("UnstableApiUsage") // See https://github.com/PaperMC/Paper/discussions/10972#discussioncomment-10093821
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftPrepare(final CrafterCraftEvent event) {
        final Crafter crafter = (Crafter) event.getBlock().getState();
        for (final ItemStack item : crafter.getInventory()) {
            if (item != null && containsClaimType(item) == true) {
                event.setCancelled(true);
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

    // Prevents item from being used in stone cutter recipe.
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

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE)
            return;
        // Getting destination location of the portal. While this could technically fail because we ONLY check the first block of the portal, it should still cover 99.9% cases.
        final Location location = event.getBlocks().getFirst().getLocation();
        // Adapting Bukkit's World object to WorldEdit one.
        final com.sk89q.worldedit.world.World aWorld = BukkitAdapter.adapt(location.getWorld());
        // Getting the RegionManager instance for involved world.
        final @Nullable RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(aWorld);
        // Returning if no RegionManager was found for the involved world.
        if (regions == null)
            return;
        // Iterating over all claims at the destination area.
        for (final ProtectedRegion region : regions.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
            // Cancelling the event if non-player entity tries to create a portal on ANY protected region.
            if (event.getEntity() instanceof Player == false) {
                event.setCancelled(true);
                return;
            } else if (event.getEntity() instanceof Player player) {
                final UUID uniqueId = player.getUniqueId();
                // Disallowing portal creation if player is neither an owner nor a member of the region.
                if (player.hasPermission("claims.plugin.can_modify_unowned_claim") == false && region.getMembers().contains(uniqueId) == false && region.getOwners().contains(uniqueId) == false) {
                    Message.of(PluginLocale.PORTAL_TELEPORT_FAILURE).sendActionBar(player);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    // Needs to be low priority for WorldGuard's greeting / farewell flags to be properly cancelled / ignored.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortalTeleport(final PlayerTeleportEvent event) {
        // Returning if portal rules are disabled in the configuration.
        if (PluginConfig.PORTALS_TELEPORT_TO_UNAUTHORIZED_REGIONS == true || event.getFrom().getWorld().key().asString().equals("minecraft:the_end") == true)
            return;
        // Checking if teleport cause was nether portal or end portal.
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL)
            return;
        // Getting destination location of the teleport.
        final Location location = event.getTo();
        // Adapting Bukkit's World object to WorldEdit one.
        final com.sk89q.worldedit.world.World aWorld = BukkitAdapter.adapt(location.getWorld());
        // Getting the RegionManager instance for involved world.
        final @Nullable RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(aWorld);
        // Returning if no RegionManager was found for the involved world.
        if (regions == null)
            return;
        // Iterating over all claims at the destination area.
        for (final ProtectedRegion region : regions.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
            final UUID uniqueId = event.getPlayer().getUniqueId();
            // Disallowing teleportation if player is neither an owner nor a member of the region.
            if (event.getPlayer().hasPermission("claims.plugin.can_modify_unowned_claim") == false && region.getMembers().contains(uniqueId) == false && region.getOwners().contains(uniqueId) == false) {
                Message.of(PluginLocale.PORTAL_TELEPORT_FAILURE).sendActionBar(event.getPlayer());
                event.setCancelled(true);
                return;
            }
        }
    }

    private static boolean containsClaimType(final @NotNull ItemStack item) {
        return item.hasItemMeta() == true && item.getItemMeta().getPersistentDataContainer().has(Claims.Key.CLAIM_TYPE, STRING) == true;
    }

    // Likely to be incomplete.
    private static boolean isBlockOrSummonsEntity(final @NotNull ItemStack item) {
        return item.getType().isBlock() == true
                || item.getType() == Material.ARMOR_STAND
                || item.getType() == Material.ITEM_FRAME
                || item.getType() == Material.GLOW_ITEM_FRAME
                || MaterialTags.SPAWN_EGGS.isTagged(item)
                || Tag.ITEMS_BOATS.isTagged(item.getType());
    }

}

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
package cloud.grabsky.claims.commands;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseCategories;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanelOpen;
import static cloud.grabsky.claims.util.Utilities.toChunkPosition;
import static java.util.Comparator.comparingInt;

@SuppressWarnings("UnstableApiUsage")
@Command(name = "claims", aliases = {"claim"}, permission = "claims.command.claims", usage = "/claims (...)")
public class ClaimsCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Claims plugin;

    @Dependency
    private @UnknownNullability ClaimManager claimManager;

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        // Getting the first argument (second input element) from command input.
        final String argument = context.getInput().at(1, "").toLowerCase();
        // Displaying list of sub-commands in case no argument has been provided.
        if (index == 0)
            return CompletionsProvider.filtered(it -> context.getExecutor().hasPermission(this.getPermission() + "." + it), "border", "get", "list", "reload", "restore", "teleport");
        // Otherwise, checking permissions and sending specialized permissions to the sender.
        return (context.getExecutor().hasPermission(this.getPermission() + "." + argument) == true)
                ? switch (argument) {
                    case "list" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
                    case "restore", "teleport" -> (index == 1) ? CompletionsProvider.of(Claim.class) : CompletionsProvider.EMPTY;
                    // Displaying no completions in case unrecognized argument has been provided.
                    default -> CompletionsProvider.EMPTY;
                }
                // Displaying no completions in case command executor is not authorized to use that sub-command.
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Displaying help in case no arguments has been provided.
        if (arguments.hasNext() == false) {
            this.onDefault(context, arguments);
        // Otherwise, executing specialized sub-command logic.
        } else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "get" -> this.onClaimsGet(context, arguments);
            case "list" -> this.onClaimsList(context, arguments);
            case "border" -> this.onClaimsBorder(context, arguments);
            case "reload" -> this.onClaimsReload(context, arguments);
            case "restore" -> this.onClaimsRestore(context, arguments);
            case "teleport" -> this.onClaimsTeleport(context, arguments);
            // Displaying help in case unrecognized argument has been provided.
            default -> this.onHelp(context);
        }
    }


    /* CLAIMS */

    private void onDefault(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
        // Getting location of the command sender.
        final Location location = sender.getLocation();
        // Getting Claim at location of the command sender.
        final @Nullable Claim claim = claimManager.getClaimAt(location);
        // ...
        if (claim != null) {
            // Checking whether sender is owner of that claim or has ability to edit claims of other players.
            if (claimSender.isOwnerOf(claim) == true || sender.hasPermission(this.getPermission() + ".edit") == true) {
                // Checking whether the claim panel is already open.
                if (isClaimPanelOpen(claim) == false) {
                    // Building new instance of ClaimPanel and opening it to the command sender.
                    new ClaimPanel.Builder()
                            .setClaimManager(claimManager)
                            .setClaim(claim)
                            .build()
                            .open(sender, (panel) -> {
                                plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseCategories.INSTANCE, false));
                                return true;
                            });
                    return;
                }
                // Sending error message to command sender.
                Message.of(PluginLocale.CLAIMS_ALREADY_EDITING).send(sender);
                return;
            } else if (claimSender.isMemberOf(claim) == true) {
                new ClaimPanel.Builder()
                        .setClaimManager(claimManager)
                        .setAccessBlockLocation(null)
                        .build().open(sender, (panel) -> {
                            if (panel instanceof ClaimPanel cPanel) {
                                plugin.getBedrockScheduler().run(1L, (task) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, false));
                                return true;
                            }
                            return false;
                        });
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.NOT_CLAIM_MEMBER).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
    }


    /* CLAIMS HELP */

    private void onHelp(final @NotNull RootCommandContext context) {
        Message.of(PluginLocale.COMMAND_CLAIMS_USAGE).send(context.getExecutor());
    }


    /* CLAIMS LIST */

    private static final ExceptionHandler.Factory CLAIMS_LIST_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_LIST_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    @Experimental // TO-DO: Fix UUID arguments reporting invalid players for new players (may have something to do with #hasPlayedBefore check)
    private void onClaimsList(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            final OfflinePlayer target = (sender instanceof Player senderPlayer)
                    ? arguments.next(OfflinePlayer.class).asOptional(senderPlayer)
                    : arguments.next(OfflinePlayer.class).asRequired(CLAIMS_LIST_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".list.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting ClaimPlayer instance of specified player.
            final ClaimPlayer cTarget = claimManager.getClaimPlayer(target.getUniqueId());
            // Getting list of claims owned by specified player.
            final Set<Claim> ownedClaims = cTarget.getClaims();
            // Sending specialized message in case target is not owner of any claim.
            if (ownedClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_NONE).placeholder("player", target).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_HEADER)
                        .placeholder("player", target)
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                ownedClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_LIST_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .placeholder("claim_owner", claim.getOwners().getFirst().toUser().getName())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_FOOTER)
                        .placeholder("player", target)
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
            }
            // Getting list of claims specified player is member of.
            final Set<Claim> relativeClaims = cTarget.getRelativeClaims();
            // Sending specialized message in case target is not member of any claim.
            if (relativeClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_NONE).placeholder("player", target).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_HEADER)
                        .placeholder("player", target)
                        .placeholder("count", relativeClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                relativeClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_LIST_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .placeholder("claim_owner", claim.getOwners().getFirst().toUser().getName())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_FOOTER)
                        .placeholder("player", target.getName())
                        .placeholder("count", relativeClaims.size())
                        .send(sender);
            }
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* CLAIMS BORDER */

    private static final ItemStack BORDER_ITEM = new ItemStack(Material.COAL, 1);
    private static final NamespacedKey BORDER_ITEM_MODEL = new NamespacedKey("wireframe", "wireframe_white");

    static {
        BORDER_ITEM.setData(DataComponentTypes.ITEM_MODEL, BORDER_ITEM_MODEL);
    }

    private void onClaimsBorder(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".border") == true) {
            final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
            // Removing current border entities.
            if (claimSender.getBorderEntities().isEmpty() == false) {
                final int[] currentEntities = claimSender.getBorderEntities().stream().mapToInt(Integer::intValue).toArray();
                // Creating the packet.
                final var destroyPacket = new WrapperPlayServerDestroyEntities(currentEntities);
                // Sending packet to the player.
                PacketEvents.getAPI().getPlayerManager().sendPacket(sender, destroyPacket);
                // Clearing list of border entities.
                claimSender.getBorderEntities().clear();
                // Sending message to the player and returning.
                Message.of(PluginLocale.CLAIM_BORDER_DISABLED).sendActionBar(sender);
                return;
            }
            // Getting location of the command sender.
            final Location location = sender.getLocation();
            // Getting Claim at location of the command sender.
            final @Nullable Claim claim = claimManager.getClaimAt(location);
            // ..............
            if (claim == null) {
                Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
                return;
            }
            // ...
            final var minY = location.getWorld().getMinHeight();
            final var maxY = location.getWorld().getMaxHeight();
            // Calculating total number of entities that are going to be created.
            // First location must be 5 blocks below the min Y, because 10 is added to Y coordinate on first iteration of the for loop.
            final Location initialLocation = new Location(claim.getCenter().getWorld(), claim.getCenter().x(), minY - 5, claim.getCenter().z());
            // ...
            for (int i = minY; i <= maxY + 1; i += 10) {
                final Location spawnLocation = initialLocation.add(0, 10, 0);
                // ...
                final com.github.retrooper.packetevents.protocol.world.Location wrappedLocation = SpigotConversionUtil.fromBukkitLocation(spawnLocation);
                // Getting the next entity ID...
                final int id = Bukkit.getUnsafe().nextEntityId();
                // Adding to the set...
                claimSender.getBorderEntities().add(id);
                // Creating packet...
                final var spawnPacket = new WrapperPlayServerSpawnEntity(id, UUID.randomUUID(), EntityTypes.ITEM_DISPLAY, wrappedLocation, 0, 0, null);
                final var metadataPacket = new WrapperPlayServerEntityMetadata(id, List.of(
                        new EntityData(12, EntityDataTypes.VECTOR3F, new Vector3f(claim.getType().getRadius() * 2 + 1, 10, claim.getType().getRadius() * 2 + 1)),
                        new EntityData(17, EntityDataTypes.FLOAT, 2.0F),
                        new EntityData(23, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(BORDER_ITEM))
                ));
                PacketEvents.getAPI().getPlayerManager().sendPacket(sender, spawnPacket);
                PacketEvents.getAPI().getPlayerManager().sendPacket(sender, metadataPacket);
            }
            Message.of(PluginLocale.CLAIM_BORDER_ENABLED).sendActionBar(sender);
            plugin.getBedrockScheduler().run(40L, (task) -> {
                Message.of(PluginLocale.CLAIM_BORDER_SHADERS_NOT_SUPPORTED).sendActionBar(sender);
            });
        }
    }


    /* CLAIMS GET */

    private void onClaimsGet(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".get") == true) {
            if (claimManager.getClaimTypes().isEmpty() == false) {
                // Iterating over list of claims.
                claimManager.getClaimTypes().values().stream().sorted(comparingInt(Claim.Type::getRadius)).forEach(type -> {
                    // Getting identifier of this claim type.
                    final String id = type.getId();
                    // Getting block item of this claim type.
                    final ItemStack blockItem = type.getBlock();
                    // Adding the block item to sender's inventory.
                    sender.getInventory().addItem(
                            new ItemBuilder(blockItem).setPersistentData(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING, id).build()
                    );
                });
                // Sending success message to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_GET_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.COMMAND_CLAIMS_GET_FAILURE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* CLAIMS RELOAD */

    private void onClaimsReload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".reload") == true) {
            // Reloading the claims cache...
            claimManager.cacheClaims();
            // Reloading the plugin...
            if (plugin.onReload() == true) {
                // Sending success message to the sender.
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* CLAIMS RESTORE */

    private void onClaimsRestore(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".restore") == true) {
            // Getting next argument as Claim. Defaults to the claim player is currently on. Might be null.
            final @Nullable Claim claim = arguments.next(Claim.class).asOptional(claimManager.getClaimAt(sender.getLocation()));
            // Sending error message if no claim argument was provided and player is not on any claim.
            if (claim == null) {
                Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
                return;
            }
            // Trying...
            try {
                // Getting center location of the claim.
                final Location center = claim.getCenter();
                // Getting type of claim block of the claim.
                final Material type = claim.getType().getBlock().getType();
                // Getting chunk at claim center asynchronously...
                center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> {
                    // Getting claim center position in chunk.
                    final BlockPosition position = toChunkPosition(center);
                    // Restoring claim block at center position of this claim.
                    chunk.getBlock(position.blockX(), position.blockY(), position.blockZ()).setType(type);
                    // Sending success message to the sender.
                    Message.of(PluginLocale.COMMAND_CLAIMS_RESTORE_SUCCESS).send(sender);
                });
            } catch (final ClaimProcessException e) {
                // Sending error message to the sender.
                Message.of(e.getErrorMessage()).send(sender);
                // Logging error message to the console
                claimManager.getPlugin().getLogger().warning("An error occurred while trying to access claim:");
                claimManager.getPlugin().getLogger().warning("   " + e.getMessage());
            }
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* CLAIMS TELEPORT */

    private void onClaimsTeleport(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            // Getting next argument as Claim. Defaults to the claim player is currently on. Might be null.
            final @Nullable Claim claim = arguments.next(Claim.class).asOptional(claimManager.getClaimAt(sender.getLocation()));
            // Sending error message if no claim argument was provided and player is not on any claim.
            if (claim == null) {
                Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
                return;
            }
            // Trying...
            try {
                // Getting center location of the claim.
                final Location destination = claim.getCenter().clone().add(0.0, 0.5, 0.0);
                // Getting chunk at claim center asynchronously...
                sender.teleportAsync(destination).thenAccept(chunk -> {
                    // Sending success message to the sender.
                    Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(sender);
                });
            } catch (final ClaimProcessException e) {
                // Sending error message to the sender.
                Message.of(e.getErrorMessage()).send(sender);
                // Logging error message to the console
                claimManager.getPlugin().getLogger().warning("An error occurred while trying to access claim:");
                claimManager.getPlugin().getLogger().warning("   " + e.getMessage());
            }
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

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
import cloud.grabsky.claims.commands.argument.WaypointArgument;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "waypoints", aliases = {"waypoint"}, permission = "claims.command.waypoints", usage = "/waypoints (...)")
public final class WaypointCommand extends RootCommand {

    @Dependency
    private @UnknownNullability WaypointManager waypointManager;


    @Override // TO-DO: Some work need to be put into improving WaypointArgument. Right now it doesn't help with anything, especially when using for completions.
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        // Getting the first argument (second input element) from command input.
        final String argument = context.getInput().at(1, "").toLowerCase();
        // Displaying list of sub-commands in case no argument has been provided.
        if (index == 0)
            return CompletionsProvider.filtered(it -> context.getExecutor().hasPermission(this.getPermission() + "." + it) == true, "create", "remove", "list", "teleport");
        // Otherwise, checking permissions and sending specialized permissions to the sender.
        return (context.getExecutor().hasPermission(this.getPermission() + "." + argument) == true)
                ? switch (argument) {
                    case "create", "list" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
                    case "remove", "teleport" -> switch (index) {
                        case 1 -> CompletionsProvider.of(Player.class);
                        case 2 -> {
                            final String value = context.getInput().at(index);
                            // ...
                            final Player player = value.equalsIgnoreCase("@self") ? context.getExecutor().asPlayer() : Bukkit.getPlayerExact(value);
                            yield (player != null) ? WaypointArgument.of(waypointManager, player.getUniqueId()) : CompletionsProvider.EMPTY;
                        }
                        // Displaying no completions for higher indexes.
                        default -> CompletionsProvider.EMPTY;
                    };
                    // Displaying no completions in case unrecognized argument has been provided.
                    default -> CompletionsProvider.EMPTY;
                }
                // Displaying no completions in case command executor is not authorized to use that sub-command.
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Displaying help in case no arguments has been provided.
        if (arguments.hasNext() == false)
            this.onDefault(context);
        // Otherwise, executing specialized sub-command logic.
        else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "create"   -> this.onWaypointCreate(context, arguments);
            case "remove"   -> this.onWaypointRemove(context, arguments);
            case "list"     -> this.onWaypointList(context, arguments);
            case "teleport" -> this.onWaypointTeleport(context, arguments);
            // Displaying help in case unrecognized argument has been provided.
            default -> this.onDefault(context);
        }
    }


    /* WAYPOINTS */

    private void onDefault(final @NotNull RootCommandContext context) {
        Message.of(PluginLocale.COMMAND_WAYPOINTS_USAGE).send(context.getExecutor());
    }


    /* WAYPOINTS CREATE */

    private static final ExceptionHandler.Factory WAYPOINTS_CREATE_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_CREATE_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    private void onWaypointCreate(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".create") == true) {
            // Getting target.
            final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(WAYPOINTS_CREATE_USAGE);
            // Getting waypoint name.
            final String name = arguments.next(String.class).asRequired(WAYPOINTS_CREATE_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".create.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Creating the waypoint.
            final Waypoint waypoint = Waypoint.fromCommand(target.getUniqueId(), name, sender.getLocation());
            // ...
            waypointManager.createWaypoint(target.getUniqueId(), waypoint).thenAccept(isSuccess -> {
                // Sending success/error message to command sender.
                Message.of(isSuccess == true ? PluginLocale.COMMAND_WAYPOINTS_CREATE_SUCCESS : PluginLocale.COMMAND_WAYPOINTS_CREATE_FAILURE_ALREADY_EXISTS)
                        .placeholder("name", name)
                        .send(sender);
            });
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WAYPOINTS LIST */

    private static final ExceptionHandler.Factory WAYPOINTS_LIST_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    private void onWaypointList(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            // Getting the target. Player executors may leave this argument empty.
            final OfflinePlayer target = (sender instanceof Player senderPlayer)
                    ? arguments.next(OfflinePlayer.class).asOptional(senderPlayer)
                    : arguments.next(OfflinePlayer.class).asRequired(WAYPOINTS_LIST_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".list.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting waypoints of specified target.
            final List<Waypoint> waypoints = waypointManager.getWaypoints(target);
            // Sending specialized message in case target does not have any waypoints.
            if (waypoints.isEmpty() == true) {
                Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_NONE).placeholder("player", target).send(sender);
                return;
            }
            // Sending output header to the sender.
            Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_HEADER)
                    .placeholder("player", target)
                    .placeholder("count", waypoints.size())
                    .send(sender);
            // Iterating over waypoints and listing each of them to the sender.
            for (var waypoint : waypointManager.getWaypoints(target.getUniqueId())) {
                final @Nullable Location location = waypoint.getLocation().complete();
                // ...
                Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_ENTRY)
                        .replace("<target>", target.getName()) // Uses Message#replace because placeholders does not work inside click events.
                        .replace("<waypoint_name>", waypoint.getName()) // Uses Message#replace because placeholders does not work inside click events.
                        .placeholder("waypoint_location", (location != null)
                                ? Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ())
                                : Component.text("N/A")
                        )
                        .placeholder("waypoint_displayname", waypoint.getDisplayName())
                        .send(sender);
            }
            // Sending output footer to the sender.
            Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_FOOTER).placeholder("count", waypoints.size()).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }


    /* WAYPOINTS REMOVE */

    private static final ExceptionHandler.Factory WAYPOINTS_REMOVE_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    private void onWaypointRemove(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".remove") == true) {
            // Getting arguments...
            final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(WAYPOINTS_REMOVE_USAGE);
            final String name = arguments.next(String.class).asRequired(WAYPOINTS_REMOVE_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".remove.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting player's waypoint with given name.
            final @Nullable Waypoint waypoint = waypointManager.getFirstWaypoint(target.getUniqueId(), (w) -> w.getName().equals(name) == true);
            // Sending error message to command sender, in case waypoint with such name does not exist.
            if (waypoint == null) {
                Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_FAILURE_NOT_FOUND).placeholder("name", name).send(sender);
                return;
            }
            // Destroying the waypoint.
            waypoint.destroy(waypointManager);
            // Sending success message to command sender.
            Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_SUCCESS).placeholder("name", name).send(sender);
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* WAYPOINTS TELEPORT */

    private static final ExceptionHandler.Factory WAYPOINTS_TELEPORT_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_TELEPORT_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    private void onWaypointTeleport(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            // Getting arguments...
            final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(WAYPOINTS_TELEPORT_USAGE);
            final Waypoint waypoint = arguments.next(Waypoint.class, WaypointArgument.of(waypointManager, target.getUniqueId())).asRequired(WAYPOINTS_TELEPORT_USAGE);
            // In case specified target is not sender, checking permissions.
            if (target != sender && sender.hasPermission(this.getPermission() + ".teleport.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            final @Nullable Location location = waypoint.getLocation().complete();
            // ...
            if (location == null) {
                System.out.println("WaypointCommand#onWaypointTeleport: LOCATION NOT FOUND");
                return;
            }
            // Teleporting sender to location of the waypoint.
            sender.teleportAsync(location).thenAccept(isSuccess -> {
                // Sending success message to command sender.
                Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(sender);
            });
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

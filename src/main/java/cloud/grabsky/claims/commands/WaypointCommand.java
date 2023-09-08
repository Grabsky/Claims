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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.stream.Stream;

@Command(name = "waypoints", aliases = {"waypoint"}, permission = "claims.command.waypoints", usage = "/waypoints (...)")
public final class WaypointCommand extends RootCommand {

    @Dependency
    private @UnknownNullability WaypointManager waypointManager;

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (index == 0)
            return CompletionsProvider.of(Stream.of("create", "remove", "list", "teleport").filter(it -> sender.hasPermission(this.getPermission() + "." + it)).toList());
        // ...
        final String literal = context.getInput().at(1).toLowerCase();
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // ...
        return switch (literal) {
            case "create", "list" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
            case "teleport", "remove" -> switch (index) {
                case 1 -> CompletionsProvider.of(Player.class);
                case 2 -> {
                    final String value = context.getInput().at(index);
                    // ...
                    final Player player = value.equalsIgnoreCase("@self") ? context.getExecutor().asPlayer() : Bukkit.getPlayerExact(value);
                    yield (player != null)
                            ? WaypointArgument.of(waypointManager, player.getUniqueId())
                            : CompletionsProvider.EMPTY;
                }
                default -> CompletionsProvider.EMPTY;
            };
            default -> CompletionsProvider.EMPTY;
        };
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false)
            this.onDefault(context);
        // ...
        else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "create" -> this.onWaypointCreate(context, arguments);
            case "remove" -> this.onWaypointRemove(context, arguments);
            case "list" -> this.onWaypointList(context, arguments);
            case "teleport" -> this.onWaypointTeleport(context, arguments);
            // Displaying help when unrecognized argument has been found.
            default -> this.onDefault(context);
        }
    }

    private void onDefault(final @NotNull RootCommandContext context) {
        Message.of(PluginLocale.COMMAND_WAYPOINTS_USAGE).send(context.getExecutor());
    }

    /* WAYPOINTS CREATE */

    private static final ExceptionHandler.Factory WAYPOINTS_CREATE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_CREATE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWaypointCreate(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".create") == true) {
            // Getting target.
            final Player target = arguments.next(Player.class).asRequired(WAYPOINTS_CREATE_USAGE);
            // Getting waypoint name.
            final String name = arguments.next(String.class).asRequired(WAYPOINTS_CREATE_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".create.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Creating the waypoint.
            waypointManager.createWaypoint(target.getUniqueId(), name, Waypoint.Source.COMMAND, sender.getLocation()).thenAccept((isSuccess) -> {
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

    /* WAYPOINTS REMOVE */

    private static final ExceptionHandler.Factory WAYPOINTS_REMOVE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWaypointRemove(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".remove") == true) {
            // Getting arguments...
            final OfflinePlayer target = arguments.next(Player.class).asRequired(WAYPOINTS_REMOVE_USAGE);
            final String name = arguments.next(String.class).asRequired(WAYPOINTS_REMOVE_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".remove.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Trying...
            try {
                // Removing all waypoints (owned by specified player) with given name.
                waypointManager.removeAllWaypoints(target.getUniqueId(), (waypoint) -> waypoint.getName().equals(name) == true);
                // Sending success message to command sender.
                Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_SUCCESS).placeholder("name", name).send(sender);
            } catch (final IllegalArgumentException ___) {
                // Sending error message to command sender.
                Message.of(PluginLocale.COMMAND_WAYPOINTS_REMOVE_FAILURE_NOT_FOUND).placeholder("name", name).send(sender);
            }
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* WAYPOINTS LIST */

    private static final ExceptionHandler.Factory WAYPOINTS_LIST_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWaypointList(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            // Getting the target. Player executors may leave this argument empty.
            final Player target = (context.getExecutor().isPlayer() == true)
                    ? arguments.next(Player.class).asOptional((Player) sender)
                    : arguments.next(Player.class).asRequired(WAYPOINTS_LIST_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".list.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting waypoints of specified target.
            final List<Waypoint> waypoints = waypointManager.getWaypoints(target);
            // Sending specialized message in case target does not have any waypoints.
            if (waypoints.isEmpty() == true) {
                Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_EMPTY).send(sender);
                return;
            }
            // Sending output header to the sender.
            Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_HEADER)
                    .placeholder("player", target)
                    .placeholder("count", waypoints.size())
                    .send(sender);
            // Iterating over waypoints and listing each of them to the sender.
            for (var waypoint : waypointManager.getWaypoints(target.getUniqueId())) {
                Message.of(PluginLocale.COMMAND_WAYPOINTS_LIST_ENTRY)
                        .replace("<target>", target.getName()) // Uses Message#replace because placeholders does not work inside click events.
                        .replace("<waypoint_name>", waypoint.getName()) // Uses Message#replace because placeholders does not work inside click events.
                        .placeholder("waypoint_location", waypoint.getLocation())
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

    /* WAYPOINTS TELEPORT */

    private static final ExceptionHandler.Factory WAYPOINTS_TELEPORT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_WAYPOINTS_TELEPORT_USAGE).send(context.getExecutor());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onWaypointTeleport(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            // Getting arguments...
            final OfflinePlayer target = arguments.next(Player.class).asRequired(WAYPOINTS_TELEPORT_USAGE);
            final Waypoint waypoint = arguments.next(Waypoint.class, WaypointArgument.of(waypointManager, target.getUniqueId())).asRequired(WAYPOINTS_TELEPORT_USAGE);
            //
            if (target != sender && sender.hasPermission(this.getPermission() + ".teleport.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // ...
            sender.teleportAsync(waypoint.getLocation()).thenAccept(isSuccess -> {
                Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(sender);
            });
            return;
        }
        // Sending error message to command sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

package cloud.grabsky.claims.commands;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.commands.argument.WaypointArgument;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public final class WaypointCommand extends RootCommand {

    private final WaypointManager waypointManager;

    public WaypointCommand(final Claims plugin) {
        super("waypoint", null, "claims.command.waypoint", "/waypoint (...)", "...");
        this.waypointManager = plugin.getClaimManager().getWaypointManager();
    }

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
            case "create" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
            case "remove" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.of("...");
            case "list" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
            case "teleport" -> switch (index) {
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
        // ...
        if (arguments.hasNext() == false)
            this.onDefault(context);
        // ...
        else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "create" -> this.onWaypointCreate(context, arguments);
            case "remove" -> this.onWaypointRemove(context, arguments);
            case "list" -> this.onWaypointList(context, arguments);
            case "teleport" -> this.onWaypointTeleport(context, arguments);
        }
    }

    private void onDefault(final @NotNull RootCommandContext context) {
        context.getExecutor().asCommandSender().sendMessage("DEFAULT");
    }

    private void onWaypointCreate(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".create") == true) {
            // ...
            final Player target = arguments.next(Player.class).asRequired();
            final String name = arguments.next(String.class).asRequired();
            // ...
            if (target != sender && sender.hasPermission(this.getPermission() + ".create.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // ...
            if (waypointManager.getWaypoints(target.getUniqueId()).size() >= PluginConfig.WAYPOINTS_LIMIT) {
                Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_REACHED_WAYPOINTS_LIMIT).send(sender);
                return;
            }
            // ...
            if (waypointManager.createWaypoint(target.getUniqueId(), name, Waypoint.Source.COMMAND, sender.getLocation()) == true) {
                Message.of(PluginLocale.WAYPOINT_PLACE_SUCCESS).placeholder("name", name).send(sender);
                return;
            }
            // ...
            Message.of(PluginLocale.WAYPOINT_PLACE_FAILURE_ALREADY_EXISTS).placeholder("name", name).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWaypointRemove(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        // ...
    }

    private void onWaypointList(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            // ...
            final Player target = arguments.next(Player.class).asRequired();
            // ...
            for (var waypoint : waypointManager.getWaypoints(target.getUniqueId())) {
                sender.sendMessage(waypoint.getName() + " / " + waypoint.getLocation().x() + ", " + waypoint.getLocation().y() + ", " + waypoint.getLocation().z());
            }
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onWaypointTeleport(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".teleport") == true) {
            // ...
            final Player target = arguments.next(Player.class).asRequired();
            final Waypoint waypoint = arguments.next(Waypoint.class, WaypointArgument.of(waypointManager, target.getUniqueId())).asRequired();
            // ...
            if (target != sender && sender.hasPermission(this.getPermission() + ".teleport.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // ...
            sender.teleportAsync(waypoint.getLocation());
            Message.of("Teleporting...").send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

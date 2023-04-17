package cloud.grabsky.claims.commands.argument;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WaypointArgument implements CompletionsProvider, ArgumentParser<Waypoint> {

    private final WaypointManager waypointManager;
    private final UUID uuid;

    public static WaypointArgument of(final @NotNull WaypointManager waypointManager, final @NotNull UUID uniqueId) {
        return new WaypointArgument(waypointManager, uniqueId);
    }


    @Override
    public @NotNull List<String> provide(@NotNull final RootCommandContext context) throws CommandLogicException {
        return waypointManager.getWaypoints(uuid).stream().map(Waypoint::getName).toList();
    }

    @Override
    public Waypoint parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        // ...
        final @Nullable Waypoint waypoint = waypointManager.getWaypoints(uuid).stream().filter((w) -> w.getName().equals(value) == true).findFirst().orElse(null);
        // ...
        if (waypoint != null)
            return waypoint;
        // ...
        throw new WaypointArgument.Exception(value);
    }

    public static final class Exception extends ArgumentParseException {

        public Exception(final String inputValue) {
            super(inputValue);
        }

        public Exception(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final @NotNull RootCommandContext context) {
            Message.of("Waypoint " + this.getInputValue() + " has not been found.").send(context.getExecutor().asCommandSender());
        }

    }

}

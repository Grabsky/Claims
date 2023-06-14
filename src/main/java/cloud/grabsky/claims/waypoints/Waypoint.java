package cloud.grabsky.claims.waypoints;

import io.papermc.paper.math.Position;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Waypoint {

    public enum Source { COMMAND, BLOCK }

    public Waypoint(
            final @NotNull String id,
            final @NotNull String displayName,
            final @NotNull UUID owner,
            final @NotNull Source source,
            final @NotNull Long createdOn,
            final @NotNull Location location
    ) {
        this.name = id;
        this.displayName = displayName;
        this.owner = owner;
        this.source = source;
        this.createdOn = createdOn;
        this.location = location;
    }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod = @__({@Internal}))
    private String displayName;

    @Getter(AccessLevel.PUBLIC)
    private transient final UUID owner;

    @Getter(AccessLevel.PUBLIC) @Setter(value = AccessLevel.PUBLIC, onMethod = @__({@Internal}))
    private transient boolean isPendingRename = false;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Source source;

    @Getter(AccessLevel.PUBLIC)
    private final long createdOn;

    private final @NotNull Location location;

    public @NotNull Location getLocation() {
        return location.clone();
    }

    @Experimental @SuppressWarnings("UnstableApiUsage") // Experimental status inherited from Paper's Position API.
    public static @NotNull String createDefaultName(final @NotNull Position position) {
        return position.x() + "_" + position.y() + "_" + position.z();
    }

}

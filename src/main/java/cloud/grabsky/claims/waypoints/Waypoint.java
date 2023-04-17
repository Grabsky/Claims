package cloud.grabsky.claims.waypoints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Waypoint {

    public enum Source {
        COMMAND,
        BLOCK
    }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Source source;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Location location;

    public Waypoint(final @NotNull Source source, final @NotNull Location location) {
        this("x" + location.blockX() + "y" + location.blockY() + "z" + location.blockZ(), source, location);
    }

}

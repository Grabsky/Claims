package cloud.grabsky.claims.waypoints;

import com.squareup.moshi.Json;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Waypoint {

    public enum Source { COMMAND, BLOCK }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Source source;

    @Getter(AccessLevel.PUBLIC) @Json(name = "created_on")
    private final long createdOn;

    private final @NotNull Location location;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private transient boolean isStale = false;

    public @NotNull Location getLocation() {
        return location.clone();
    }

}

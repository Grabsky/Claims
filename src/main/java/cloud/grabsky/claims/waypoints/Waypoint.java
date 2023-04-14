package cloud.grabsky.claims.waypoints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Waypoint {

    @Getter(AccessLevel.PUBLIC)
    private final String name;

    @Getter(AccessLevel.PUBLIC)
    private final Location location;

}

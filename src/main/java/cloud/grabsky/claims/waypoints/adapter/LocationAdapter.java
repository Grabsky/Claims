package cloud.grabsky.claims.waypoints.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static com.squareup.moshi.Types.getRawType;

public enum LocationAdapter implements JsonAdapter.Factory {
    /* SINGLETON */ INSTANCE;

    @Override
    public @Nullable JsonAdapter<Location> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
        if (Location.class.isAssignableFrom(getRawType(type)) == false)
            return null;
        // ...
        final var adapter = moshi.adapter(LocationSurrogate.class);
        // ...
        return new JsonAdapter<>() {

            @Override
            public @Nullable Location fromJson(final @NotNull JsonReader in) throws IOException {
                final LocationSurrogate surrogate = adapter.fromJson(in);
                // ...
                return (surrogate != null) ? surrogate.toLocation() : null;
            }

            @Override
            public void toJson(final @NotNull JsonWriter out, @Nullable final Location value) throws IOException {
                if (value != null) {
                    final LocationSurrogate surrogate = LocationSurrogate.fromLocation(value);
                    adapter.toJson(out, surrogate);
                }
            }

        };

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LocationSurrogate {

        private final NamespacedKey world;
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;

        public static LocationSurrogate fromLocation(final Location location) {
            return new LocationSurrogate(location.getWorld().getKey(), location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
        }

        public Location toLocation() throws IllegalArgumentException {
            final World bWorld = Bukkit.getServer().getWorld(world);
            // ...
            if (bWorld == null)
                throw new IllegalArgumentException("World " + world.asString() + " is not loaded.");
            // ...
            return new Location(bWorld, x, y, z, yaw, pitch);
        }

    }

}

package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.waypoints.adapter.LocationAdapter;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.squareup.moshi.Types.newParameterizedType;
import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class WaypointManager {

    private final Claims plugin;
    private final Moshi moshi;
    private final File waypointsDirectory;

    @Getter(AccessLevel.PUBLIC)
    private final Map<UUID, List<Location>> cache;

    public WaypointManager(final Claims plugin) {
        this.plugin = plugin;
        this.moshi = new Moshi.Builder()
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(LocationAdapter.INSTANCE)
                .build();
        this.waypointsDirectory = new File(plugin.getDataFolder(), "waypoints");
        this.cache = new HashMap<>();
    }

    @Internal
    public void loadCache() throws IOException {
        // Creating cache directory if does not exist.
        if (waypointsDirectory.exists() == false)
            waypointsDirectory.mkdirs();
        // ...
        if (waypointsDirectory.isDirectory() == false)
            throw new IllegalStateException(waypointsDirectory.getPath() + " is not a directory.");
        // ...
        final File[] files = waypointsDirectory.listFiles();
        // ...
        if (files == null) {
            plugin.getLogger().info("No waypoints were loaded: " + waypointsDirectory.getPath() + " is empty.");
            return;
        }
        int count = 0;
        // ...
        for (final File file : files)
            if (file.getName().endsWith(".json") == true) {
                final UUID uniqueId = UUID.fromString(file.getName().split("\\.")[0]);
                // ...
                final List<Location> waypoints = this.loadWaypoints(file);
                // ...
                cache.put(uniqueId, waypoints);
                // ...
                count += 1;
            }
        // ...
        plugin.getLogger().info(count + " waypoints were loaded from cache.");
    }

    public void createWaypoint(final UUID uuid, final Location location) {
        cache.get(uuid).add(location);
        // ...
        plugin.getBedrockScheduler().runAsync(1L, (task) -> {
            // Creating directory in case it does not exist.
            if (waypointsDirectory.exists() == false)
                waypointsDirectory.mkdirs();
            // Throwing an exception in case file is not a directory.
            if (waypointsDirectory.isDirectory() == false)
                throw new IllegalStateException(waypointsDirectory.getPath() + " is not a directory.");
            // ...
            final File file = new File(waypointsDirectory, uuid + ".json");
            // ...
            try (final JsonWriter writer = JsonWriter.of(buffer(sink(file)))) {
                // Writing data to the file
                moshi.adapter(newParameterizedType(List.class, Location.class)).toJson(writer, cache.get(uuid));
                // ...
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<Location> loadWaypoints(final @NotNull File file) {
        // ...
        if (file.exists() == false)
            return new ArrayList<>();
        // ...
        try (final JsonReader reader = JsonReader.of(buffer(source(file)))) {
            final List<Location> location = (List<Location>) moshi.adapter(newParameterizedType(List.class, Location.class)).fromJson(reader);
            // ...
            reader.close();
            // ...
            if (location == null)
                return new ArrayList<>();
            // ...
            return location;
        } catch (final IOException e) {
            plugin.getLogger().warning("Waypoint cannot be loaded. (FILE = " + file.getPath() + ")");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}

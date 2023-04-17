package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.waypoints.adapter.LocationAdapter;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
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
    private final Map<UUID, List<Waypoint>> cache;

    private static final Type listOfWaypoint = newParameterizedType(List.class, Waypoint.class);

    public WaypointManager(final Claims plugin) {
        this.plugin = plugin;
        this.moshi = new Moshi.Builder()
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(LocationAdapter.INSTANCE)
                .build();
        this.waypointsDirectory = new File(plugin.getDataFolder(), "waypoints");
        this.cache = new HashMap<>();
        // ...
        this.loadCache();
    }

    public List<Waypoint> getWaypoints(final @NotNull UUID uniqueId) {
        return cache.getOrDefault(uniqueId, new ArrayList<>());
    }

    @Internal
    public void loadCache() {
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
                final List<Waypoint> waypoints = this.loadWaypoints(file);
                // ...
                cache.put(uniqueId, waypoints);
                // ...
                count += 1;
            }
        // ...
        plugin.getLogger().info(count + " waypoints were loaded from cache.");
    }

    public boolean createWaypoint(final @NotNull UUID uuid, final @Nullable String name, final @NotNull Waypoint.Source source, final @NotNull Location location) {
        // Returning 'false' when waypoint with similar name is found.
        for (var waypoint : cache.getOrDefault(uuid, new ArrayList<>()))
            if (waypoint.getName().equalsIgnoreCase(name) == true)
                return false;
        // ...
        final Waypoint waypoint = (name != null) ? new Waypoint(name, source, location) : new Waypoint(source, location);
        // ...
        cache.computeIfAbsent(uuid, (u) -> new ArrayList<>()).add(waypoint);
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
                moshi.adapter(listOfWaypoint).toJson(writer, cache.getOrDefault(uuid, new ArrayList<>()));
                // ...
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
        // ...
        return true;
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<Waypoint> loadWaypoints(final @NotNull File file) {
        // ...
        if (file.exists() == false)
            return new ArrayList<>();
        // ...
        try (final JsonReader reader = JsonReader.of(buffer(source(file)))) {
            final List<Waypoint> waypoints = (List<Waypoint>) moshi.adapter(listOfWaypoint).fromJson(reader);
            // ...
            reader.close();
            // ...
            if (waypoints == null)
                return new ArrayList<>();
            // ...
            return waypoints;
        } catch (final IOException e) {
            plugin.getLogger().warning("Waypoint cannot be loaded. (FILE = " + file.getPath() + ")");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}

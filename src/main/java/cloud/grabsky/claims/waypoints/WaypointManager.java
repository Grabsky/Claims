package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.waypoints.adapter.LocationAdapter;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import okio.BufferedSink;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.squareup.moshi.Types.newParameterizedType;
import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class WaypointManager {

    private final Claims plugin;
    private final Moshi moshi;
    private final File waypointsDirectory;
    private final ConcurrentMap<UUID, List<Waypoint>> cache;

    private static final Type TYPE_LIST_OF_WAYPOINTS = newParameterizedType(List.class, Waypoint.class);

    public WaypointManager(final Claims plugin) {
        this.plugin = plugin;
        this.moshi = new Moshi.Builder()
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(LocationAdapter.INSTANCE)
                .build();
        this.waypointsDirectory = new File(plugin.getDataFolder(), "waypoints");
        this.cache = new ConcurrentHashMap<>();
        // ...
        this.loadCache();
    }

    public List<Waypoint> getWaypoints(final @NotNull UUID uniqueId) {
        return cache.getOrDefault(uniqueId, new ArrayList<>());
    }

    @Internal
    public void loadCache() {
        ensureCacheDirectoryExists();
        // ...
        final File[] files = waypointsDirectory.listFiles();
        // ...
        if (files == null) {
            plugin.getLogger().info("No waypoints were loaded: " + waypointsDirectory.getPath() + " is empty.");
            return;
        }
        int count = 0;
        // ...
        for (final File file : files) {
            if (file.getName().endsWith(".json") == true) {
                final UUID uniqueId = UUID.fromString(file.getName().split("\\.")[0]);
                // ...
                final List<Waypoint> waypoints = this.loadFile(file);
                // ...
                cache.put(uniqueId, waypoints);
                // ...
                count += waypoints.size();
            }
        }
        // ...
        plugin.getLogger().info(count + " waypoints were loaded from cache.");
    }

    @SuppressWarnings("UnstableApiUsage")
    private static @NotNull BlockPosition toChunkPosition(final Position position) {
        return Position.block((position.blockX() & 0xF), position.blockY(), (position.blockZ() & 0xF));
    }

    public boolean hasWaypoint(final @NotNull UUID uniqueId, final @NotNull String name) {
        if (cache.containsKey(uniqueId) == false)
            return false;
        // ...
        for (final Waypoint waypoint : cache.get(uniqueId))
            if (waypoint.getName().equals(name) == true)
                return true;
        // ...
        return false;
    }

    public @NotNull CompletableFuture<Boolean> createWaypoint(final @NotNull UUID uuid, final @NotNull String name, final @NotNull Waypoint.Source source, final @NotNull Location location) throws IllegalArgumentException {
        // Returning 'false' when waypoint with similar name is found.
        if (this.hasWaypoint(uuid, name) == true)
            throw new IllegalArgumentException("Player " + uuid + " already has a waypoint named " + name + ".");
        // ...
        final Waypoint waypoint = new Waypoint(name, source, System.currentTimeMillis(), location);
        // ...
        cache.computeIfAbsent(uuid, (u) -> new ArrayList<>()).add(waypoint);
        // ...
        return this.save(uuid);
    }

    public CompletableFuture<Boolean> removeWaypoint(final @NotNull UUID uuid, final @NotNull Location location) {
        final List<Waypoint> waypointsCopy = (cache.containsKey(uuid) == true) ? new ArrayList<>(cache.get(uuid)) : new ArrayList<>();
        // ...
        waypointsCopy.removeIf((waypoint) -> waypoint.getLocation().equals(location) == true);
        // ...
        if (cache.getOrDefault(uuid, new ArrayList<>()).size() == waypointsCopy.size())
            return CompletableFuture.completedFuture(false);
        // ...
        cache.put(uuid, waypointsCopy);
        // ...
        return this.save(uuid);
    }

    public @NotNull CompletableFuture<Boolean> save(final @NotNull UUID uuid) {
        ensureCacheDirectoryExists();
        // ...
        final File file = new File(waypointsDirectory, uuid + ".json");
        // ...
        return CompletableFuture.supplyAsync(() -> {
            // Non-existent data does not need to be saved...
            if (cache.containsKey(uuid) == false)
                return true;
            // ...
            final List<Waypoint> waypoints = cache.get(uuid);
            // ...
            try (final BufferedSink buffer = buffer(sink(file))) {
                // ...
                moshi.adapter(TYPE_LIST_OF_WAYPOINTS).indent("  ").toJson(buffer, waypoints);
                // ...
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<Waypoint> loadFile(final @NotNull File file) {
        // ...
        if (file.exists() == false)
            return new ArrayList<>();
        // ...
        try (final JsonReader reader = JsonReader.of(buffer(source(file)))) {
            final List<Waypoint> waypoints = (List<Waypoint>) moshi.adapter(TYPE_LIST_OF_WAYPOINTS).fromJson(reader);
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

    private void ensureCacheDirectoryExists() throws IllegalStateException {
        // Creating directory in case it does not exist.
        if (waypointsDirectory.exists() == false)
            waypointsDirectory.mkdirs();
        // Throwing an exception in case file is not a directory.
        if (waypointsDirectory.isDirectory() == false)
            throw new IllegalStateException(waypointsDirectory.getPath() + " is not a directory.");
    }

}

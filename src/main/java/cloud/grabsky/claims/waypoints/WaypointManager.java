package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.waypoints.adapter.LocationAdapter;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static com.squareup.moshi.Types.newParameterizedType;
import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class WaypointManager {

    private final Claims plugin;
    private final File dataDirectory;
    private final ConcurrentMap<UUID, List<Waypoint>> cache;

    private final JsonAdapter<Object> adapter; // JsonAdapter<List<Waypoint>>

    private static final Type TYPE_LIST_OF_WAYPOINTS = newParameterizedType(List.class, Waypoint.class);

    public WaypointManager(final @NotNull Claims plugin) {
        this.plugin = plugin;
        this.dataDirectory = new File(plugin.getDataFolder(), "waypoints");
        this.cache = new ConcurrentHashMap<>();
        // ...
        this.adapter = new Moshi.Builder()
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
                .add(LocationAdapter.INSTANCE)
                .build().adapter(TYPE_LIST_OF_WAYPOINTS).indent("  "); // JsonAdapter<List<Waypoint>>
        // Caching waypoints.
        this.cacheWaypoints();
    }

    @Internal
    public void cacheWaypoints() throws IllegalStateException {
        // Creating data directory if does not exist.
        ensureDataDirectoryExists();
        // Getting list of the files within the cache directory. Non-recursive.
        final File[] files = dataDirectory.listFiles();
        // ...
        int loadedPlayersCount = 0;
        int loadedWaypointsCount = 0;
        // Iterating over each file...
        for (final File file : (files != null) ? files : new File[0]) {
            // Skipping non-JSON files.
            if (file.getName().endsWith(".json") == true) {
                try {
                    // Unboxing file name to (owner) UUID object.
                    final UUID uniqueId = UUID.fromString(file.getName().split("\\.")[0]);
                    // Increasing number of total players.
                    // Loading list of waypoints from the file.
                    final List<Waypoint> waypoints = this.readFile(file);
                    // Increasing number of loaded players.
                    loadedPlayersCount++;
                    // Adding to the cache...
                    cache.put(uniqueId, waypoints);
                    // Increasing number of loaded waypoints.
                    loadedWaypointsCount += waypoints.size();
                } catch (final IOException | IllegalStateException e) {
                    plugin.getLogger().warning("Waypoint(s) cannot be loaded. (FILE = " + file.getPath() + ")");
                    e.printStackTrace();
                }
            }
        }
        // Printing "summary" message to the console.
        plugin.getLogger().info("Successfully loaded " + loadedWaypointsCount + " waypoint(s) owned by " + loadedPlayersCount + " player(s) total.");
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<Waypoint> readFile(final @NotNull File file) throws IOException, IllegalStateException {
        // Creating a JsonReader from provided file.
        final JsonReader reader = JsonReader.of(buffer(source(file)));
        // Reading the JSON file.
        final List<Waypoint> waypoints = (List<Waypoint>) adapter.fromJson(reader);
        // Closing the reader.
        reader.close();
        // Throwing exception in case List<Waypoint> ended up being null. Unlikely to happen, but possible.
        if (waypoints == null)
            throw new IllegalArgumentException("Deserialization of " + file.getPath() + " failed: " + null);
        // ...
        return waypoints;
    }

    public @NotNull CompletableFuture<Boolean> createWaypoint(final @NotNull UUID uniqueId, final @NotNull String name, final @NotNull Waypoint.Source source, final @NotNull Location location) {
        // Creating Waypoint object using provided values, or overriding existing one.
        final Waypoint waypoint = cache.getOrDefault(uniqueId, new ArrayList<>()).stream()
                .filter(w -> w.getName().equalsIgnoreCase(name) == true)
                .findFirst().orElse(new Waypoint(name, source, System.currentTimeMillis(), location));
        // Adding waypoint to the cache.
        cache.computeIfAbsent(uniqueId, (___) -> new ArrayList<>()).add(waypoint);
        // Saving and returning the result.
        return this.save(uniqueId);
    }

    public @NotNull CompletableFuture<Boolean> removeWaypoint(final @NotNull UUID uniqueId, final @NotNull Predicate<Waypoint> predicate) throws IllegalArgumentException {
        // Creating a copy of waypoints owned by specified player.
        final List<Waypoint> waypointsCopy = (cache.containsKey(uniqueId) == true) ? new ArrayList<>(cache.get(uniqueId)) : new ArrayList<>();
        // Removing waypoint(s) matching provided location.
        waypointsCopy.removeIf(predicate);
        // Returning "failed" CompletableFuture in case nothing was removed from the list.
        if ((cache.containsKey(uniqueId) == true ? cache.get(uniqueId).size() : 0) == waypointsCopy.size())
            throw new IllegalArgumentException("No waypoints matching predicate were removed"); // TO-DO: Improve message.
        // Updating the cache.
        cache.put(uniqueId, waypointsCopy);
        // Saving and returning the result.
        return this.save(uniqueId);
    }

    public @NotNull CompletableFuture<Boolean> save(final @NotNull UUID uniqueId) {
        ensureDataDirectoryExists();
        // ...
        final File file = new File(dataDirectory, uniqueId + ".json");
        // Returning CompletableFuture which saves the file asynchronously.
        return CompletableFuture.supplyAsync(() -> {
            // Returning 'true' in case there is no data to save.
            if (cache.containsKey(uniqueId) == false)
                return true;
            // Getting data currently held in cache.
            final List<Waypoint> waypoints = cache.get(uniqueId);
            // ...
            try (final JsonWriter writer = JsonWriter.of(buffer(sink(file)))) {
                // Writing data to the file.
                adapter.toJson(writer, waypoints);
                // Returning 'true' assuming data was written.
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
                return false;
            }
        }).exceptionally(thr -> {
            thr.printStackTrace();
            return false;
        });
    }

    public @NotNull @Unmodifiable List<Waypoint> getWaypoints(final @NotNull UUID uniqueId) {
        return (cache.containsKey(uniqueId) == true) ? Collections.unmodifiableList(cache.get(uniqueId)) : Collections.emptyList();
    }

    public @NotNull @Unmodifiable List<Waypoint> getWaypoints(final @NotNull Player player) {
        return this.getWaypoints(player.getUniqueId());
    }

    public boolean hasWaypoint(final @NotNull UUID uniqueId, final @NotNull String name) {
        for (final Waypoint waypoint : (cache.containsKey(uniqueId) == true) ? cache.get(uniqueId) : new ArrayList<Waypoint>())
            if (waypoint.getName().equals(name) == true)
                return true;
        // No waypoints found. Returning false.
        return false;
    }


    /**
     * Returns {@link BlockPosition} containing chunk position of provided {@link Position}.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull BlockPosition toChunkPosition(final Position position) {
        return Position.block((position.blockX() & 0xF), position.blockY(), (position.blockZ() & 0xF));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull NamespacedKey toChunkDataKey(final @NotNull BlockPosition chunkPosition) {
        return new NamespacedKey("claims", "waypoint_" + chunkPosition.blockX() + "_" + chunkPosition.blockY() + "_" + chunkPosition.blockZ());
    }

    /**
     * Ensures that cache directory exists. In case file is not a directory - it gets deleted and a directory is created in its place.
     */
    private void ensureDataDirectoryExists() throws IllegalStateException {
        // Creating directory in case it does not exist.
        if (dataDirectory.exists() == false)
            dataDirectory.mkdirs();
        // Deleting and re-creating in case file is not a directory.
        if (dataDirectory.isDirectory() == false) {
            if (dataDirectory.delete() == false)
                throw new IllegalStateException("File " + dataDirectory.getPath() + " is not a directory and could not be deleted. Please delete or rename it manually.");
            // Calling (self) after deleting non-directory file. This should not lead to inifnite recursion.
            ensureDataDirectoryExists();
        }
    }

}

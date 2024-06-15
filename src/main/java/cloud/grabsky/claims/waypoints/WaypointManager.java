/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.util.InstanceCreator;
import cloud.grabsky.claims.util.Utilities;
import cloud.grabsky.configuration.paper.adapter.NamespacedKeyAdapter;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import lombok.AccessLevel;
import lombok.Getter;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;
import static com.squareup.moshi.Types.newParameterizedType;
import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

public final class WaypointManager {

    @Getter(AccessLevel.PUBLIC)
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
                .add(InstanceCreator.createFactory(WaypointManager.class, () -> this))
                .add(NamespacedKey.class, NamespacedKeyAdapter.INSTANCE)
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
                    // Completing...
                    waypoints.replaceAll(waypoint -> waypoint.complete(uniqueId));
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

    /**
     * Reads file containing waypoints.
     */
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

    /**
     * Creates a waypoint for the specified {@link UUID} with the given parameters, then attempts to save changes to the filesystem.
     */
    public @NotNull CompletableFuture<Boolean> createWaypoint(final @NotNull UUID uniqueId, @NotNull Waypoint waypoint) {
        final String name = waypoint.getName();
        // Creating Waypoint object using provided values, or overriding existing one.
        waypoint = cache.getOrDefault(uniqueId, new ArrayList<>()).stream()
                .filter(cachedWaypoint -> cachedWaypoint.getName().equalsIgnoreCase(name) == true)
                .findFirst().orElse(waypoint);
        // Adding waypoint to the cache.
        cache.computeIfAbsent(uniqueId, (___) -> new ArrayList<>()).add(waypoint);
        // Saving and returning the result.
        return this.save(uniqueId);
    }

    /**
     * Removes a waypoint owned by the specified {@link UUID}, then attempts to save changes to the filesystem.
     */
    public @NotNull CompletableFuture<Boolean> removeWaypoints(final @NotNull UUID uniqueId, final @NotNull Waypoint waypoint) throws IllegalArgumentException {
        return removeWaypoints(uniqueId, (cached) -> cached.equals(waypoint) == true);
    }

    /**
     * Removes all waypoints owned by the specified {@link UUID} that match the provided {@link Predicate}, then attempts to save changes to the filesystem.
     */
    public @NotNull CompletableFuture<Boolean> removeWaypoints(final @NotNull UUID uniqueId, final @NotNull Predicate<Waypoint> predicate) throws IllegalArgumentException {
        // Creating a copy of waypoints owned by specified player.
        final List<Waypoint> waypointsCopy = (cache.containsKey(uniqueId) == true) ? new ArrayList<>(cache.get(uniqueId)) : new ArrayList<>();
        // Removing waypoint(s) matching provided location.
        waypointsCopy.removeIf(predicate);
        // Returning "failed" CompletableFuture in case nothing was removed from the list.
        if ((cache.containsKey(uniqueId) == true ? cache.get(uniqueId).size() : 0) == waypointsCopy.size())
            throw new IllegalArgumentException("No waypoints matching predicate were removed.");
        // Updating the cache.
        cache.put(uniqueId, waypointsCopy);
        // Saving and returning the result.
        return this.save(uniqueId);
    }

    /**
     * Saves waypoints owned by the specified {@link UUID} to the filesystem.
     */
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

    public @NotNull @Unmodifiable List<Waypoint> getWaypoints(final @NotNull OfflinePlayer player) {
        return this.getWaypoints(player.getUniqueId());
    }

    public @NotNull @Unmodifiable List<Waypoint> getWaypoints(final @NotNull UUID uniqueId) {
        return (cache.containsKey(uniqueId) == true) ? Collections.unmodifiableList(cache.get(uniqueId)) : Collections.emptyList();
    }

    public @Nullable Waypoint getBlockWaypoint(final @NotNull Location location) {
        return cache.values().stream().flatMap(Collection::stream).filter(waypoint -> {
            return waypoint.getSource() == Waypoint.Source.BLOCK && Utilities.equalsNonNull(waypoint.getLocation().complete(), location);
        }).findFirst().orElse(null);
    }

    public @Nullable Waypoint getFirstWaypoint(final @NotNull UUID uniqueId, final @NotNull Predicate<Waypoint> predicate) {
        // Iterating over list of cached waypoints.
        for (final Waypoint waypoint : (cache.containsKey(uniqueId) == true) ? cache.get(uniqueId) : new ArrayList<Waypoint>())
            // Returning first Waypoint that matches given Predicate.
            if (predicate.test(waypoint) == true) return waypoint;
        // No waypoints found. Returning null.
        return null;
    }

    public boolean hasWaypoint(final @NotNull UUID uniqueId, final @NotNull String name) {
        return this.getFirstWaypoint(uniqueId, (waypoint) -> waypoint.getName().equals(name) == true) != null;
    }

    public @NotNull CompletableFuture<Boolean> renameWaypoint(final UUID uniqueId, final Waypoint waypoint, final @NotNull String newDisplayName) {
        final String transformed = newDisplayName.trim().replace("  ", " ");
        // Validating...
        if (inRange(transformed.length(), 1, 32) == true) {
            // Making sure currently "edited" waypoint still exists...
            if (this.hasWaypoint(uniqueId, waypoint.getName()) == true) {
                waypoint.setDisplayName(transformed);
                // Saving...
                return this.save(uniqueId);
            }
        }
        // Returning false...
        return CompletableFuture.completedFuture(false);
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
            // Calling (self) after deleting non-directory file. This should not lead to infinite recursion.
            ensureDataDirectoryExists();
        }
    }

}

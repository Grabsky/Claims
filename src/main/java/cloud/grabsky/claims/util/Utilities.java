package cloud.grabsky.claims.util;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.commands.exception.NumberParseException;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utilities {

    public static <T> ListIterator<T> moveIterator(final ListIterator<T> iterator, final int nextIndex) {
        while (iterator.nextIndex() != nextIndex) {
            if (iterator.nextIndex() < nextIndex)
                iterator.next();
            else if (iterator.nextIndex() > nextIndex)
                iterator.previous();
        }
        return iterator;
    }

    public static <T extends Number> @UnknownNullability T getNumberOrDefault(final @NotNull Supplier<T> supplier, final @Nullable T def) {
        try {
            return supplier.get();
        } catch (final NullPointerException | NumberFormatException | NumberParseException e) {
            return def;
        }
    }

    @Experimental
    @SuppressWarnings("UnstableApiUsage") // Status inherited from Paper's Position API.
    public static @NotNull Stream<BlockPosition> getAroundPosition(final @NotNull BlockPosition position, final int radius) {
        return new ArrayList<BlockPosition>((int) Math.pow((radius + 1) * 2, 3)) {{
            final int centerX = position.blockX();
            final int centerY = position.blockY();
            final int centerZ = position.blockZ();
            // ...
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                        add(Position.block(x, y, z));
                    }
                }
            }
        }}.stream();
    }

    // NOTE: Safe teleports may (and probably should) be introduced in the future. Teleport invulnerability is a temporary solution.
    public static void teleport(final @NotNull HumanEntity source, final @NotNull Location destination, final int delay, final @Nullable String bypassPermission, final @Nullable BiConsumer<Location, Location> then) {
        // Handling teleports with no (or bypassed) delay.
        if (bypassPermission != null && source.hasPermission(bypassPermission) == true) {
            final Location old = source.getLocation();
            // ...
            source.teleportAsync(destination, TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                if (isSuccess == false) {
                    Message.of(PluginLocale.TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                    return;
                }
                // Sending success message through action bar.
                Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(source);
                // Returning if no effects were provided or player is vanished. (vanish checks compatible only with Azure)
                if (then == null)
                    return;
                // Scheduling task that spawns provided effects.
                Claims.getInstance().getBedrockScheduler().run(1L, (task) -> {
                    // Making player invulnerable for next 5 seconds.
                    source.setNoDamageTicks(100);
                    // Executing provided task.
                    then.accept(old, destination);
                });
            });
            return;
        }
        // Handling delayed teleports.
        final Location sourceInitialLocation = source.getLocation();
        // Sending action bar message with delay information.
        Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delay).sendActionBar(source);
        // Scheduling a repeating task every 1 second, until specified numbers of iterations is reached.
        Claims.getInstance().getBedrockScheduler().repeat(20L, 20L, (delay - 1), (cycle) -> {
            // Sending action bar message with delay information.
            Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delay - cycle).sendActionBar(source);
            // Handling teleport interrupt. (moving)
            if (source.getLocation().distanceSquared(sourceInitialLocation) > 1.0) {
                Message.of(PluginLocale.TELEPORT_FAILURE_MOVED).sendActionBar(source);
                return false;
            }
            // Handling last iteration.
            if (cycle == delay) {
                final Location old = source.getLocation();
                // ...
                source.teleportAsync(destination, TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                    if (isSuccess == false) {
                        Message.of(PluginLocale.TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                        return;
                    }
                    // Sending success message through action bar.
                    Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(source);
                    // Returning if no effects were provided.
                    if (then == null)
                        return;
                    // Scheduling task that spawns provided effects.
                    Claims.getInstance().getBedrockScheduler().run(1L, (task) -> {
                        // Making player invulnerable for next 5 seconds.
                        source.setNoDamageTicks(100);
                        // Executing provided task.
                        then.accept(old, destination);
                    });
                });
            }
            return true;
        });

    }

    /**
     * Returns {@link BlockPosition} containing chunk position of provided {@link Position}.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull BlockPosition toChunkPosition(final Position position) {
        return Position.block((position.blockX() & 0xF), position.blockY(), (position.blockZ() & 0xF));
    }


    private static final Random RANDOM = new Random();

    /**
     * Returns random {@link Location} in the specified radius.
     */
    public static Location getRandomLocationInSquare(final @NotNull Location center, final int minDistance, final int maxDistance) {
        // ...
        int x = RANDOM.nextInt(center.getBlockX() - maxDistance, center.getBlockX() + maxDistance);
        int z = RANDOM.nextInt(center.getBlockZ() - maxDistance, center.getBlockZ() + maxDistance);
        // ...
        if (Math.abs(x) <= minDistance && Math.abs(z) <= minDistance)
            if (Math.abs(x) <= minDistance)
                x = (x > 0) ? x + minDistance : x - minDistance;
            else if (Math.abs(z) <= minDistance)
                z = (z > 0) ? z + minDistance : z - minDistance;
        // ...
        return new Location(center.getWorld(), x, 0, z);
    }

    private static final HashSet<Biome> OCEAN_BIOMES = new HashSet<>(Arrays.asList(
            Biome.OCEAN,
            Biome.COLD_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.WARM_OCEAN,
            Biome.DEEP_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN
    ));

    /**
     * Tries to find `attempts` number of `Locations` and returns the first one to be safe.
     */
    public static CompletableFuture<Location> getSafeLocation(final int minRadius, final int maxRadius) {
        final CompletableFuture<Location> future = new CompletableFuture<>();
        // ...
        final AtomicInteger attempts = new AtomicInteger(0);
        // ...
        Claims.getInstance().getBedrockScheduler().runAsync(1L, (task) -> {
            while (future.isDone() == false) {
                System.out.println(attempts + " attempts.");
                // ...
                attempts.set(attempts.get() + 1);
                // ...
                final Location location = getRandomLocationInSquare(PluginConfig.DEFAULT_WORLD.getSpawnLocation(), minRadius, maxRadius);
                // ...
                final Chunk chunk = location.getChunk();
                // ...
                if (OCEAN_BIOMES.contains(location.getWorld().getBiome(location)) == true)
                    continue;
                // ...
                location.set(
                        location.getBlockX() + 0.5D,
                        location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1.0D,
                        location.getBlockZ() + 0.5D
                );
                // ...
                if (isSafe(chunk, location) == true)
                    future.complete(location);
            }
        });
        // ...
        return future;
    }

    /**
     * Returns whether this `Location` is safe.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isSafe(final @NotNull Chunk chunk, final @NotNull Location location) {
        for (int x = location.getBlockX() - 3; x <= location.getBlockX() + 3; x++) {
            for (int y = location.getBlockY() - 3; y <= location.getBlockY() + 3; y++) {
                for (int z = location.getBlockZ() - 3; z <= location.getBlockZ() + 3; z++) {
                    final BlockPosition pos = toChunkPosition(Position.block(x, y, z));
                    if (chunk.getBlock(pos.blockX(), pos.blockY(), pos.blockZ()).isLiquid() == true) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if both objects are equal and not-null, otherwise {@code false} is returned.
     */
    public static boolean equalsNonNull(final @Nullable Object first, final @Nullable Object second) {
        // Returning 'false' in case both values are null.
        if (first == null && second == null)
            return false;
        // Comparing and returning the result otherwise.
        return Objects.equals(first, second);
    }

    private static final org.bukkit.util.Vector BOTTOM = new Vector(0, -1, 0);

    @SuppressWarnings("UnstableApiUsage")
    public static @Nullable Block findFirstBlockUnder(final Location location, final int depth, final Material type) {
        // Raytracing blocks. This is likely to be less performent than classic for-loop, but definitely cleaner and more readable.
        final @Nullable RayTraceResult result = location.getWorld().rayTraceBlocks(location, BOTTOM, depth, FluidCollisionMode.NEVER, true, (block) -> block.getType() == type);
        // Returning the result Block instance or null.
        return (result != null) ? result.getHitBlock() : null;
    }

}

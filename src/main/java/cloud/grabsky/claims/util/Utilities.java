/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.util;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.commands.exception.NumberParseException;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(Extensions.class)
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

    // Players with this permission can teleport instantly.
    public static final String BYPASS_TELEPORT_COOLDOWN = "claims.plugin.bypass_teleport_cooldown";

    private static void showFadeIn(final Audience audience) {
        // Sending the title, but only if configured to do so.
        if (PluginConfig.TELEPORTATION_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION.isBlank() == false)
            audience.showRichTitle(Component.translatable(PluginConfig.TELEPORTATION_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION), Component.empty(), 8, 16, 8);
    }

    /**
     * Performs a teleportation of specified {@link HumanEntity} to specified {@link Location}.
     */
    private static CompletableFuture<Boolean> performTeleport(final @NotNull HumanEntity source, final @NotNull Location destination) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        // Fading-in the black screen.
        showFadeIn(source);
        // Scheduling teleport 8 ticks after fading-in player's screen. So that teleport is hidden.
        Claims.getInstance().getBedrockScheduler().run(8L, (_) -> {
            source.teleportAsync(destination, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                // Fading-out the black screen.
                if (PluginConfig.TELEPORTATION_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION.isBlank() == false)
                    source.fadeOutTitle(8, 8);
                // ...
                if (isSuccess) {
                    // Showing success message on the action bar.
                    Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(source);
                    // Making player invulnerable for next 5 seconds.
                    source.setNoDamageTicks(100);
                }
                // Completing the future.
                future.complete(isSuccess);
            });
        });
        // Returning the future.
        return future;
    }

    /**
     * Performs a teleportation of specified {@link Player} to specified {@link Location}.
     * For delays greater than 0, action bar countdown is generated. Smooth fade-in/fade-out animations are included via {@link Utilities#performTeleport(HumanEntity, Location) Utilities#performTeleport}.
     */
    public static void teleport(final @NotNull Player source, final @NotNull Location destination, final int delay, final @Nullable BiPredicate<Location, Location> shouldCancel, final @Nullable TriConsumer<Boolean, Location, Location> then) {
        // Getting the initial position of the player.
        final Location sourceInitialLocation = source.getLocation();
        // Handling teleports with no (or bypassed) delay.
        if (delay == 0 || source.hasPermission(BYPASS_TELEPORT_COOLDOWN) == true) {
            // Fading-in to the black screen, teleporting asynchronously, sending messages and fading-out.
            performTeleport(source, destination).thenAccept(isSuccess -> {
                if (isSuccess == true && then != null) then.accept(isSuccess, sourceInitialLocation, destination);
            });
            // Returning, as this has been already handled.
            return;
        }
        // Sending action bar message with delay information.
        Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delay).sendActionBar(source);
        // Submitting an asynchronous countdown task, after which the player will be teleported.
        CompletableFuture.supplyAsync(() -> {
                    try {
                        for (int delayLeft = delay; delayLeft != 0; delayLeft--) {
                            source.playSound(source, "minecraft:block.note_block.hat", SoundCategory.MASTER, 0.5F, 2.0F);
                            // Showing message with delay information on the action bar.
                            Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delayLeft).sendActionBar(source);
                            // Handling teleport interrupt. (moving)
                            if (source.getLocation().distanceSquared(sourceInitialLocation) > 1.0) {
                                // Showing failure message on the action bar. Interrupted.
                                Message.of(PluginLocale.TELEPORT_FAILURE_MOVED).sendActionBar(source);
                                // ...
                                return false;
                            }
                            // Sleeping for one second.
                            Thread.sleep(1000);
                        }
                        // Running 'shouldCancel' predicate and cancelling the teleport in case it fails.
                        if (shouldCancel != null && shouldCancel.test(sourceInitialLocation, destination) == true) {
                            // Playing timer 'ticking' sound.
                            source.playSound(source, "minecraft:block.note_block.hat", SoundCategory.MASTER, 0.5F, 2.0F);
                            // Showing failure message on the action bar. Cancelled.
                            Message.of(PluginLocale.TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                            // ...
                            return false;
                        }
                        // The end has been reached, returning 'true' so that a teleport is attempted in the next step.
                        return true;
                    } catch (final InterruptedException _) {
                        return false;
                    }
                })
                // Fading-in to the black screen, teleporting asynchronously, sending messages and fading-out.
                .thenCompose(isSuccess -> (isSuccess == true) ? performTeleport(source, destination) : CompletableFuture.completedFuture(false))
                // Running post-teleportation tasks.
                .thenAccept(isSuccess -> {
                    if (isSuccess == true && then != null) then.accept(isSuccess, sourceInitialLocation, destination);
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
    // TO-DO: Investigate duplicated condition.
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

    /**
     * Tries to find `attempts` number of `Locations` and returns the first one to be safe.
     */
    public static CompletableFuture<Location> getSafeLocation(final int minRadius, final int maxRadius) {
        final CompletableFuture<Location> future = new CompletableFuture<>();
        final AtomicInteger attempts = new AtomicInteger(0);
        final AtomicLong start = new AtomicLong();
        // Scheduling the task to run asynchronously.
        Claims.getInstance().getBedrockScheduler().runAsync(1L, (_) -> {
            // Setting the start time.
            start.set(System.nanoTime());
            // Running a while loop until safe location is found, or 100 attempts are made. (better be safe than sorry!)
            while (future.isDone() == false && attempts.get() < 100) {
                // Incrementing attempts counter.
                attempts.set(attempts.get() + 1);
                // Getting random location in the specified radius.
                final Location location = getRandomLocationInSquare(PluginConfig.DEFAULT_WORLD.getSpawnLocation(), minRadius, maxRadius);
                // Getting chunk from the random location.
                final Chunk chunk = location.getChunk();
                // Getting the highest solid block Y at the specified location.
                final double y = getReasonablyHighY(location) + 1.0D;
                // Making sure the location is above the ground and not inside of a block.
                location.set(location.getBlockX() + 0.5D, y, location.getBlockZ() + 0.5D);
                // Getting the namespaced key of the biome.
                final String biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ()).getKey().asString();
                // Skipping blacklisted biomes
                if (PluginConfig.RANDOM_TELEPORT_BIOMES_BLACKLIST.contains(biome) == true)
                    continue;
                // Completing the future in case the location is safe.
                if (isSafe(chunk, location) == true)
                    future.complete(location);
            }
        });
        future.whenComplete((_, _) -> {
            // Logging count of attempts that were made to find a safe location. (Debugging)
            Claims.getInstance().getLogger().info("Found safe location in " + attempts + " attempts. (" + (System.nanoTime() - start.get()) / 1_000_000F + "ms)");
        });
        // Returning the future.
        return future;
    }

    /**
     * Returns highest block Y at given {@link Location} excluding non-solid blocks.
     * This method starts from the (Y = 60) and goes up until it finds a solid block.
     * If no solid block is found, then Bukkit's {@link org.bukkit.World#getHighestBlockAt(Location) World#getHighestBlock} method is used as a fallback.
     */
    public static int getReasonablyHighY(final @NotNull Location location) {
        for (int y = 60; y < location.getWorld().getMaxHeight(); y++) {
            // Getting the block at the specified location.
            final Block block = location.getWorld().getBlockAt(location.getBlockX(), y, location.getBlockZ());
            // Returning the Y coordinate in case all conditions are met. (Safe Location)
            if (block.isSolid() == true) {
                if (block.getRelative(0, 1, 0).isSolid() == false && block.getRelative(0, 2, 0).isSolid() == false)
                    if (block.getRelative(0, 2, 0).getLightFromSky() >= 2)
                        return y;
            }
        }
        // Fallback to the Bukkit method in case no safer location was found.
        return location.getWorld().getHighestBlockYAt(location);
    }

    /**
     * Returns whether this `Location` is safe.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isSafe(final @NotNull Chunk chunk, final @NotNull Location location) {
        // Returning false in case block above the location is not solid.
        if (location.getBlock().getRelative(0, 1, 0).isSolid() == true)
            return false;
        // Iterating over nearby blocks to check for any liquids.
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
        // Raytracing blocks. This is likely to be less performant than classic for-loop, but definitely cleaner and more readable.
        final @Nullable RayTraceResult result = location.getWorld().rayTraceBlocks(location, BOTTOM, depth, FluidCollisionMode.NEVER, true, (block) -> block.getType() == type);
        // Returning the result Block instance or null.
        return (result != null) ? result.getHitBlock() : null;
    }


    /**
     * Returns parsed {@link Integer} or {@code null} if invalid.
     */
    public static @Nullable Integer parseInt(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

}

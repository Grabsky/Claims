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
package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.LazyLocation;
import cloud.grabsky.claims.util.Utilities;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Waypoint {

    /**
     * Creates a new {@link Waypoint} instance of {@link Source#COMMAND Source.COMMAND} source.
     */
    @Internal
    public static @NotNull Waypoint fromCommand(final UUID owner, final String name, final Location location) {
        return new Waypoint(
                name,
                name,
                System.currentTimeMillis(),
                LazyLocation.fromLocation(location),
                Source.COMMAND
        ).complete(owner);
    }

    /**
     * Creates a new {@link Waypoint} instance of {@link Source#BLOCK Source.BLOCK} source.
     */
    @Internal
    public static @NotNull Waypoint fromBlock(final UUID owner, final String displayName, final Location location) {
        return new Waypoint(
                location.x() + "_" + location.y() + "_" + location.z(),
                displayName,
                System.currentTimeMillis(),
                LazyLocation.fromLocation(location),
                Source.BLOCK
        ).complete(owner);
    }

    @Internal
    public @NotNull Waypoint complete(final @NotNull UUID owner) {
        this.owner = owner;
        // Returning this instance, now with the owner set.
        return this;
    }

    @Getter(AccessLevel.PUBLIC)
    private transient UUID owner;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull String displayName;

    /**
     * Updates display name of this {@link Waypoint}. If applicable, this also updates decorations.
     */
    public void setDisplayName(final @NotNull String displayName) {
        this.displayName = displayName;
        // Updating decorations in case source of this waypoint is block.
        if (this.source == Source.BLOCK) {
            // Getting the Location object, this will be null in case associated world does not exist.
            final @Nullable Location location = this.location.complete();
            // Skipping waypoints that cannot be teleported to.
            if (location == null)
                return;
            // Scheduling onto the main thread...
            location.getWorld().getChunkAtAsync(location).thenAcceptAsync(chunk -> {
                // Updating TextDisplay above the block to the new display name...
                location.add(0.0F, 0.75F, 0.0F).getNearbyEntities(0.05, 0.05, 0.05).stream().filter(TextDisplay.class::isInstance).map(TextDisplay.class::cast).forEach(display -> {
                    if (display.getPersistentDataContainer().has(Claims.Key.WAYPOINT_DECORATION, PersistentDataType.BYTE) == true)
                        display.setText(this.displayName);
                });
            }, Claims.MAIN_THREAD_EXECUTOR);
        }
    }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Long createdOn;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull LazyLocation location;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Source source;

    @Getter(AccessLevel.PUBLIC)
    private transient boolean isPendingRename = false;

    /**
     * Updates state of whether this {@link Waypoint} is currently under ongoing "rename session".
     *
     * @apiNote For internal use only.
     */
    @Internal
    public void setPendingRename(final boolean state) {
        this.isPendingRename = state;
    }


    /**
     * Represents source context of how {@link Waypoint} has been created.
     */
    public enum Source {
        COMMAND, BLOCK
    }


    private static final Color TRANSPARENT = Color.fromARGB(0, 0, 0, 0);

    /**
     * Creates (saves) this {@link Waypoint} to specified {@link WaypointManager} and returns the result.
     */
    public CompletableFuture<Boolean> create(final @NotNull WaypointManager manager) {
        return manager.createWaypoint(this.owner, this).thenComposeAsync(isSuccess -> {
            // Getting the Location object, this will be null in case associated world does not exist.
            final @Nullable Location location = this.location.complete();
            // Completing with failure in case creation has failed or associated location is (somehow) invalid.
            if (isSuccess == false || location == null)
                return CompletableFuture.completedFuture(false);
            // Completing with success in case no decorations needs to be applied.
            if (this.source != Source.BLOCK)
                return CompletableFuture.completedFuture(true);
            // "Decorating" the block.
            return location.getWorld().getChunkAtAsync(location).thenApplyAsync(chunk -> {
                // Playing effects...
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
                // Creating TextDisplay above placed block.
                location.getWorld().spawnEntity(location.clone().add(0.0F, 0.75F, 0.0F), EntityType.TEXT_DISPLAY, SpawnReason.CUSTOM, (entity) -> {
                    if (entity instanceof TextDisplay display) {
                        // Setting PDC to easily distinguish from other entities.
                        display.getPersistentDataContainer().set(Claims.Key.WAYPOINT_DECORATION, PersistentDataType.BYTE, (byte) 1);
                        // Setting other visual properties.
                        display.setLineWidth(70);
                        display.setShadowed(true);
                        display.setViewRange(0.2F);
                        display.setText(this.displayName);
                        display.setBackgroundColor(TRANSPARENT);
                        display.setBillboard(Display.Billboard.CENTER);
                    }
                });
                // Returning true as decorations should now be applied.
                return true;
            }, Claims.MAIN_THREAD_EXECUTOR);
        }, Claims.MAIN_THREAD_EXECUTOR);
    }

    /**
     * Destroys and removes this {@link Waypoint} from specified {@link WaypointManager} and returns the result.
     */
    public CompletableFuture<Boolean> destroy(final @NotNull WaypointManager manager) {
        return manager.removeWaypoints(this.getOwner(), this).thenComposeAsync(isSuccess -> {
            // Getting the Location object, this will be null in case associated world does not exist.
            final @Nullable Location location = this.getLocation().complete();
            // Completing with failure in case creation has failed or associated location is (somehow) invalid.
            if (isSuccess == false || location == null)
                return CompletableFuture.completedFuture(false);
            // Invalidating sessions and closing open panels.
            manager.getPlugin().getServer().getOnlinePlayers().forEach(currPlayer -> {
                final UUID currUniqueId = currPlayer.getUniqueId();
                // Getting active session of the current player. This will be null in case no session is currently active.
                final @Nullable Session<?> session = Session.Listener.CURRENT_EDIT_SESSIONS.getIfPresent(currUniqueId);
                // Invalidating sessions...
                if (session != null) {
                    final @Nullable Location accessLocation = session.getAssociatedPanel().getAccessBlockLocation();
                    // Skipping unrelated sessions.
                    if (session.getSubject().equals(this) == true || Utilities.equalsNonNull(accessLocation, location) == true) {
                        // Invalidating the session.
                        Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(currUniqueId);
                        // Clearing the title.
                        currPlayer.clearTitle();
                    }
                }
                // Closing open panels...
                if (currPlayer.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel cPanel) {
                    // Getting the access Location object, this will be null in case of trigger by command.
                    final @Nullable Location accessLocation = cPanel.getAccessBlockLocation();
                    // Closing inventory in case both locations are the same.
                    if (Utilities.equalsNonNull(accessLocation, location) == true)
                        currPlayer.closeInventory();
                }
            });
            // Completing with success in case no decorations needs to be applied.
            if (this.source != Source.BLOCK)
                return CompletableFuture.completedFuture(true);
            // "Undecorating" the block.
            return location.getWorld().getChunkAtAsync(location).thenApplyAsync(chunk -> {
                // Playing effects...
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                // Removing TextDisplay above the block.
                location.clone().add(0.0F, 0.75F, 0.0F).getNearbyEntities(0.05, 0.05, 0.05).stream().filter(TextDisplay.class::isInstance).map(TextDisplay.class::cast).forEach(display -> {
                    if (display.getPersistentDataContainer().has(Claims.Key.WAYPOINT_DECORATION, PersistentDataType.BYTE) == true)
                        display.remove();
                });
                // Removing the block.
                location.getBlock().setType(Material.AIR);
                // Returning true as decorations should now be removed.
                return true;
            }, Claims.MAIN_THREAD_EXECUTOR);
        }, Claims.MAIN_THREAD_EXECUTOR);
    }

    @Override
    public boolean equals(final @NotNull Object obj) {
        if (this == obj)
            return true;
        // ...
        if (obj instanceof Waypoint other)
            return this.name.equals(other.name) && this.owner.equals(other.owner);
        // ...
        return false;
    }

}

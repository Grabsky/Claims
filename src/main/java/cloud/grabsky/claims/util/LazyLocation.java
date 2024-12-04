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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents an immutable {@link Location} object that is not tied up to any {@link World} object.
 */
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LazyLocation {

    /**
     * Creates new {@link LazyLocation} from specified {@link Location}.
     */
    public static @NotNull LazyLocation fromLocation(final @NotNull Location location) {
        return new LazyLocation(location.getWorld().getKey(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Returns the key of the world associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final NamespacedKey world;


    /**
     * Returns the x-coordinate associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final double x;

    /**
     * Returns new instance of {@link LazyLocation} which is the same as this instance, but with {@code z} coordinate being the one specified.
     */
    public @NotNull LazyLocation withX(final int x) {
        return new LazyLocation(this.world, x, this.y, this.z, this.pitch, this.yaw);
    }

    /**
     * Returns the y-coordinate associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final double y;

    /**
     * Returns new instance of {@link LazyLocation} which is the same as this instance, but with {@code y} coordinate being the one specified.
     */
    public @NotNull LazyLocation withY(final int y) {
        return new LazyLocation(this.world, this.x, y, this.z, this.pitch, this.yaw);
    }

    /**
     * Returns the z-coordinate associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final double z;

    /**
     * Returns new instance of {@link LazyLocation} which is the same as this instance, but with {@code y} coordinate being the one specified.
     */
    public @NotNull LazyLocation withZ(final int z) {
        return new LazyLocation(this.world, this.x, this.y, z, this.pitch, this.yaw);
    }

    /**
     * Returns the yaw associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final float yaw;

    /**
     * Returns the pitch associated with this {@link LazyLocation} object.
     */
    @Getter(AccessLevel.PUBLIC)
    private final float pitch;

    /**
     * Returns {@link Location} object if the associated world exists, otherwise null.
     */
    public @Nullable Location complete() {
        return (Bukkit.getWorld(this.world) != null) ? new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch) : null;
    }

}

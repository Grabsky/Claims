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

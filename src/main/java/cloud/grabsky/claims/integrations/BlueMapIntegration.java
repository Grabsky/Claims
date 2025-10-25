/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2025  Grabsky <michal.czopek.foss@proton.me>
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
package cloud.grabsky.claims.integrations;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BlueMapIntegration {

    // Initializes this integration module.
    public static void initialize(final @NotNull Claims plugin) {
        final @Nullable World world = Bukkit.getWorld(PluginConfig.DEFAULT_WORLD.getName());
        // Returning if world ends up being null. Not really possible but needed to satisfy code analysis.
        if (world == null)
            return;
        // Hooking into BlueMaps' enable event to schedule claim markers update task.
        BlueMapAPI.onEnable(_ -> Bukkit.getAsyncScheduler().runAtFixedRate(Claims.getInstance(), (task) -> {
            // Cancelling the task if plugin is no longer enabled.
            if (Bukkit.getPluginManager().isPluginEnabled("BlueMap") == false) {
                task.cancel();
                return;
            }
            // Getting BlueMapAPI instance.
            BlueMapAPI.getInstance().ifPresent(blueMap -> {
                final MarkerSet markerSet = MarkerSet.builder().label("Claims").build();
                // Iterating over all claims and creating rectangle marker for each of them.
                plugin.getClaimManager().getClaims().forEach(claim -> {
                    final Location center = claim.getCenter();
                    final double x = center.getX();
                    final double z = center.getZ();
                    final int radius = claim.getType().getRadius();
                    // Creating rectangle marker for the current claim.
                    final Marker marker = ShapeMarker.builder()
                            .label(claim.getOwners().getFirst().toUser().getName())
                            .lineWidth(2)
                            .fillColor(new Color(0x3000FF00))
                            .lineColor(new Color(0xFF00FF00))
                            .shape(Shape.builder()
                                    .addPoints(
                                            new Vector2d(x - radius - 0.5F, z - radius - 0.5F),
                                            new Vector2d(x + radius + 0.5F, z - radius - 0.5F),
                                            new Vector2d(x + radius + 0.5F, z + radius + 0.5F),
                                            new Vector2d(x - radius - 0.5F, z + radius + 0.5F)
                                    ).build(), claim.getCenter().getWorld().getMaxHeight())
                            .build();
                    // Putting each marker inside the marker set.
                    markerSet.put(claim.getId() + ":rect", marker);
                });
                // Getting map instance and putting (overriding) claim markers.
                blueMap.getMap(world.getName()).ifPresent(it -> {
                    it.getMarkerSets().put("claims", markerSet);
                });
            });
        }, 1L, 10L, TimeUnit.SECONDS));
    }

}

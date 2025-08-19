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
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.world.World;
import org.bukkit.Location;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public final class Pl3xMapIntegration extends WorldLayer {

    private final @NotNull Claims plugin;

    private Pl3xMapIntegration(final @NotNull Claims plugin, final @NotNull String key, final @NotNull World world, final @NotNull Supplier<String> labelSupplier) {
        super(key, world, labelSupplier);
        // Setting plugin instance field.
        this.plugin = plugin;
        // Increasing default update interval to 60 seconds.
        setUpdateInterval(60);
    }

    /**
     * Initializes the Pl3xMap integration.
     * This integration registers a custom layer to display claim boundaries on the map.
     */
    public static void initialize(final @NotNull Claims plugin) {
        final net.pl3x.map.core.world.World pl3xWorld = Pl3xMap.api().getWorldRegistry().get(PluginConfig.DEFAULT_WORLD.getName());
        // Returning if world ends up being null. Not really possible but needed to satisfy code analysis.
        if (pl3xWorld == null)
            return;
        // Registering the layer.
        pl3xWorld.getLayerRegistry().register("claims", new Pl3xMapIntegration(plugin, "claims", pl3xWorld, () -> "Claims"));
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return plugin.getClaimManager().getClaims().stream().map(claim -> {
            final Location center = claim.getCenter();
            final int radius = claim.getType().getRadius();
            return Marker.rectangle(claim.getId(), Point.of(center.getX() - radius, center.getZ() - radius), Point.of(center.getX() + radius + 0.5F, center.getZ() + radius + 0.5F))
                    .setOptions(Options.builder()
                            .fillColor(0x3000FF00)
                            .strokeColor(0xFF00FF00)
                            .strokeWeight(2)
                            .tooltipContent(claim.getOwners().getFirst().toUser().getName())
                            .tooltipDirection(Tooltip.Direction.CENTER)
                            .build());
        }).collect(Collectors.toList());
    }

}

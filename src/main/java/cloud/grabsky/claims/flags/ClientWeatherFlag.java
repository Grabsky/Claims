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
package cloud.grabsky.claims.flags;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.flags.object.FixedWeather;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientWeatherFlag extends FlagValueChangeHandler<FixedWeather> {

    public ClientWeatherFlag(final Session session) {
        super(session, Claims.CustomFlag.CLIENT_WEATHER);
    }

    @Override
    protected void onInitialValue(
            final LocalPlayer localPlayer,
            final ApplicableRegionSet regionSet,
            final @Nullable FixedWeather value
    ) {
        final Player player = BukkitAdapter.adapt(localPlayer);
        if (value != null) {
            player.setPlayerWeather(value.getBukkit());
        } else {
            player.resetPlayerWeather();
        }
    }

    @Override
    protected boolean onSetValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @NotNull FixedWeather currentValue,
            final @Nullable FixedWeather lastValue,
            final MoveType moveType
    ) {
        final Player player = BukkitAdapter.adapt(localPlayer);
        // Updating...
        player.setPlayerWeather(currentValue.getBukkit());
        // Returning true as to allow movement
        return true;
    }

    @Override
    protected boolean onAbsentValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @Nullable FixedWeather lastValue,
            final MoveType moveType
    ) {
        final Player player = BukkitAdapter.adapt(localPlayer);
        // Removing...
        player.resetPlayerWeather();
        // Returning true as to allow movement
        return true;
    }

    public static final Factory<ClientWeatherFlag> FACTORY = new Factory<>() {

        @Override
        public ClientWeatherFlag create(final Session session) {
            return new ClientWeatherFlag(session);
        }

    };

}

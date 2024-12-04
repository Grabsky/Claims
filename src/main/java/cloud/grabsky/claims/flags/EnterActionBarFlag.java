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
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class EnterActionBarFlag extends FlagValueChangeHandler<String> {

    public EnterActionBarFlag(final Session session) {
        super(session, Claims.CustomFlag.ENTER_ACTIONBAR);
    }

    @Override
    protected void onInitialValue(
            final LocalPlayer localPlayer,
            final ApplicableRegionSet regionSet,
            final String value
    ) {
        // no need to re-send an action bar whenever region "refreshes"
    }

    @Override
    protected boolean onSetValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @NotNull String currentValue,
            final @Nullable String lastValue,
            final MoveType moveType
    ) {
        if (currentValue.equals(lastValue) == false)
            BukkitAdapter.adapt(localPlayer).sendActionBar(miniMessage().deserialize(currentValue));
        // ...
        return true;
    }

    @Override
    protected boolean onAbsentValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet toSet,
            final @NotNull String lastValue,
            final MoveType moveType
    ) {
        return true;
    }

    public static final Factory<EnterActionBarFlag> FACTORY = new Handler.Factory<>() {

        @Override
        public EnterActionBarFlag create(final Session session) {
            return new EnterActionBarFlag(session);
        }

    };

}

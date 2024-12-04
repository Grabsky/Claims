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

public class LeaveActionBarFlag extends FlagValueChangeHandler<String> {

    public LeaveActionBarFlag(final Session session) {
        super(session, Claims.CustomFlag.LEAVE_ACTIONBAR);
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
        // Making sure no action bar is sent when crossing border with another (non-global) region.
        if (regionSet.size() > 0)
            return true;
        // ...
        if (lastValue != null && lastValue.equals(currentValue) == false)
            BukkitAdapter.adapt(localPlayer).sendActionBar(miniMessage().deserialize(lastValue));
        // ...
        return true;
    }

    @Override
    protected boolean onAbsentValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @NotNull String lastValue,
            final MoveType moveType
    ) {
        // Can be safely assumed that Player is not null and is still online.
        BukkitAdapter.adapt(localPlayer).sendActionBar(miniMessage().deserialize(lastValue));
        // ...
        return true;
    }

    public static final Factory<LeaveActionBarFlag> FACTORY = new Handler.Factory<>() {

        @Override
        public LeaveActionBarFlag create(final Session session) {
            return new LeaveActionBarFlag(session);
        }

    };

}

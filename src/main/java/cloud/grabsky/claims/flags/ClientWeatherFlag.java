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

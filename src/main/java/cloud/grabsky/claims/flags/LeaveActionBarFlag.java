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

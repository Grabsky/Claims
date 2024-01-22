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
package cloud.grabsky.claims.configuration;

import cloud.grabsky.claims.claims.ClaimFlag;
import cloud.grabsky.claims.flags.object.FixedTime;
import cloud.grabsky.claims.flags.object.FixedWeather;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginFlags implements JsonConfiguration {

    @JsonPath("option_prefix")
    public static Component OPTION_PREFIX;

    @JsonPath("option_color")
    public static NamedTextColor OPTION_COLOR;

    @JsonPath("option_color_selected")
    public static NamedTextColor OPTION_COLOR_SELECTED;

    @JsonPath("flags.use")
    public static ClaimFlag<StateFlag.State> USE;

    @JsonPath("flags.chest-access")
    public static ClaimFlag<StateFlag.State> CHEST_ACCESS;

    @JsonPath("flags.tnt")
    public static ClaimFlag<StateFlag.State> TNT;

    @JsonPath("flags.creeper_explosion")
    public static ClaimFlag<StateFlag.State> CREEPER_EXPLOSION;

    @JsonPath("flags.snow_melt")
    public static ClaimFlag<StateFlag.State> SNOW_MELT;

    @JsonPath("flags.ice_melt")
    public static ClaimFlag<StateFlag.State> ICE_MELT;

    @JsonPath("flags.fire_spread")
    public static ClaimFlag<StateFlag.State> FIRE_SPREAD;

    @JsonPath("flags.mob_spawning")
    public static ClaimFlag<StateFlag.State> MOB_SPAWNING;

    @JsonPath("flags.client_weather")
    public static ClaimFlag<FixedWeather> CLIENT_WEATHER;

    @JsonPath("flags.client_time")
    public static ClaimFlag<FixedTime> CLIENT_TIME;

}

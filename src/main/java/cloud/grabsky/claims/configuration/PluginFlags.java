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

    @JsonPath("flags.grass_growth")
    public static ClaimFlag<StateFlag.State> GRASS_GROWTH;

    @JsonPath("flags.vine_growth")
    public static ClaimFlag<StateFlag.State> VINE_GROWTH;

    @JsonPath("flags.leaf_decay")
    public static ClaimFlag<StateFlag.State> LEAF_DECAY;



}

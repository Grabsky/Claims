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

import cloud.grabsky.claims.configuration.object.Particles;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@Internal
public final class PluginConfig implements JsonConfiguration {

    @JsonPath("claims_world")
    public static World DEFAULT_WORLD;

    @JsonPath("region_prefix")
    public static String REGION_PREFIX;

    @JsonPath("region_priority")
    public static int REGION_PRIORITY;

    @JsonPath("random_teleport_min_distance")
    public static int RANDOM_TELEPORT_MIN_DISTANCE;

    @JsonPath("random_teleport_max_distance")
    public static int RANDOM_TELEPORT_MAX_DISTANCE;

    // Teleportation

    @JsonPath("teleportation.delay")
    public static int TELEPORTATION_DELAY;

    @JsonPath("teleportation.fade_in_fade_out_animation_translation")
    public static String TELEPORTATION_FADE_IN_FADE_OUT_ANIMATION_TRANSLATION;

    @JsonNullable @JsonPath("teleportation.sounds.out")
    public static @Nullable Sound TELEPORTATION_SOUNDS_OUT;

    @JsonNullable @JsonPath("teleportation.sounds.in")
    public static @Nullable Sound TELEPORTATION_SOUNDS_IN;

    @JsonNullable @JsonPath("teleportation.particles")
    public static @Nullable List<Particles> TELEPORTATION_PARTICLES;

    // Claim Settings

    @JsonPath("claim_settings.claims_limit")
    public static int CLAIM_SETTINGS_CLAIMS_LIMIT;

    @JsonPath("claim_settings.place_attempt_cooldown")
    public static int CLAIM_SETTINGS_PLACE_ATTEMPT_COOLDOWN;

    @JsonPath("claim_settings.members_limit")
    public static int CLAIM_SETTINGS_MEMBERS_LIMIT;

    @JsonPath("claim_settings.default_display_name")
    public static String CLAIM_SETTINGS_DEFAULT_DISPLAY_NAME;

    @JsonPath("claim_settings.minimum_distance_from_spawn")
    public static int CLAIMS_SETTINGS_MINIMUM_DISTANCE_FROM_SPAWN;

    @JsonNullable @JsonPath("claim_settings.interface_upgrade_sound")
    public static Sound CLAIMS_SETTINGS_UI_UPGRADE_SOUND;

    @JsonNullable @JsonPath("claim_settings.interface_click_sound")
    public static Sound CLAIMS_SETTINGS_UI_CLICK_SOUND;

    // Claim Settings > Rename Prompt

    @JsonPath("claim_settings.rename_prompt.duration")
    public static long CLAIM_SETTINGS_RENAME_PROMPT_DURATION;

    @JsonPath("claim_settings.rename_prompt.title")
    public static Component CLAIM_SETTINGS_RENAME_PROMPT_TITLE;

    @JsonPath("claim_settings.rename_prompt.subtitle")
    public static Component CLAIM_SETTINGS_RENAME_PROMPT_SUBTITLE;

    // Waypoint Settings

    @JsonPath("waypoint_settings.enhanced_lodestone_blocks")
    public static Boolean WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS;

    @JsonPath("waypoint_settings.enhanced_lodestone_blocks_limit")
    public static int WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS_LIMIT;

    @JsonPath("waypoint_settings.default_display_name")
    public static String WAYPOINT_SETTINGS_DEFAULT_DISPLAY_NAME;

    @JsonPath("waypoint_settings.place_cooldown")
    public static int WAYPOINT_SETTINGS_PLACE_COOLDOWN;

    // Waypoint Settings > Rename Prompt

    @JsonPath("waypoint_settings.rename_prompt.duration")
    public static long WAYPOINT_SETTINGS_RENAME_PROMPT_DURATION;

    @JsonPath("waypoint_settings.rename_prompt.title")
    public static Component WAYPOINT_SETTINGS_RENAME_PROMPT_TITLE;

    @JsonPath("waypoint_settings.rename_prompt.subtitle")
    public static Component WAYPOINT_SETTINGS_RENAME_PROMPT_SUBTITLE;

}

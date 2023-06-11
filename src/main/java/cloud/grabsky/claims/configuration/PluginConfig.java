package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonNullable;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginConfig implements JsonConfiguration {

    @JsonPath("logs")
    public static boolean LOGS;

    @JsonPath("claims_world")
    public static World DEFAULT_WORLD;

    @JsonPath("region_prefix")
    public static String REGION_PREFIX;

    @JsonPath("region_priority")
    public static int REGION_PRIORITY;

    // Claim Settings

    @JsonPath("claim_settings.claims_limit")
    public static int CLAIMS_LIMIT;

    @JsonPath("claim_settings.place_attempt_cooldown")
    public static int PLACE_ATTEMPT_COOLDOWN;

    @JsonPath("claim_settings.teleport_delay")
    public static int TELEPORT_DELAY;

    @JsonPath("claim_settings.members_limit")
    public static int MEMBERS_LIMIT;

    @JsonPath("claim_settings.minimum_distance_from_spawn")
    public static int MINIMUM_DISTANCE_FROM_SPAWN;

    @JsonNullable @JsonPath("claim_settings.interface_upgrade_sound")
    public static Sound UI_UPGRADE_SOUND;

    @JsonNullable @JsonPath("claim_settings.interface_click_sound")
    public static Sound UI_CLICK_SOUND;

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

    // Waypoint Settings > Rename Prompt

    @JsonPath("waypoint_settings.rename_prompt.duration")
    public static long WAYPOINT_SETTINGS_RENAME_PROMPT_DURATION;

    @JsonPath("waypoint_settings.rename_prompt.title")
    public static Component WAYPOINT_SETTINGS_RENAME_PROMPT_TITLE;

    @JsonPath("waypoint_settings.rename_prompt.subtitle")
    public static Component WAYPOINT_SETTINGS_RENAME_PROMPT_SUBTITLE;

    // Logging Format

    @JsonPath("logging_format.claim_placed")
    public static String LOG_FORMAT_PLACED;

    @JsonPath("logging_format.claim_destroyed")
    public static String LOG_FORMAT_DESTROYED;

    @JsonPath("logging_format.claim_upgraded")
    public static String LOG_FORMAT_UPGRADED;

    @JsonPath("logging_format.claim_member_added")
    public static String LOG_FORMAT_MEMBER_ADDED;

    @JsonPath("logging_format.claim_member_removed")
    public static String LOG_FORMAT_MEMBER_REMOVED;

}

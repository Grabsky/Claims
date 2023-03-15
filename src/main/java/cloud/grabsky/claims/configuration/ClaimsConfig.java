package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.World;

public final class ClaimsConfig implements JsonConfiguration {

    @JsonPath("logs")
    public static boolean LOGS;

    @JsonPath("claims_world")
    public static World DEFAULT_WORLD;

    @JsonPath("region_prefix")
    public static String REGION_PREFIX;

    @JsonPath("region_priority")
    public static int REGION_PRIORITY;

    // Claim Settings

    @JsonPath("claim_settings.teleport_delay")
    public static int TELEPORT_DELAY;

    @JsonPath("claim_settings.members_limit")
    public static int MEMBERS_LIMIT;

    @JsonPath("claim_settings.minimum_distance_from_spawn")
    public static int MINIMUM_DISTANCE_FROM_SPAWN;

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

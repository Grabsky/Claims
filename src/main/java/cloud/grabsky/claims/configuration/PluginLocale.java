package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginLocale implements JsonConfiguration {

    // Claims

    @JsonPath("claims.player_has_no_claim")
    public static Component PLAYER_HAS_NO_CLAIM;

    @JsonPath("claims.you_dont_have_a_claim")
    public static Component YOU_DONT_HAVE_A_CLAIM;

    @JsonPath("claims.too_close_to_spawn")
    public static Component TOO_CLOSE_TO_SPAWN;

    @JsonPath("claims.overlaps_other_claim")
    public static Component OVERLAPS_OTHER_CLAIM;

    @JsonPath("claims.not_member")
    public static Component NOT_MEMBER;

    @JsonPath("claims.not_owner")
    public static Component NOT_OWNER;

    @JsonPath("claims.reached_claims_limit")
    public static Component REACHED_CLAIMS_LIMIT;

    @JsonPath("claims.place_success")
    public static Component PLACE_SUCCESS;

    @JsonPath("claims.destroy_success")
    public static Component DESTROY_SUCCESS;

    @JsonPath("claims.not_sneaking")
    public static Component NOT_SNEAKING;

    @JsonPath("claims.restore_claim_block_success")
    public static Component RESTORE_CLAIM_BLOCK_SUCCESS;

    @JsonPath("claims.restore_claim_block_fail")
    public static Component RESTORE_CLAIM_BLOCK_FAIL;

    @JsonPath("claims.claim_blocks_added")
    public static Component CLAIM_BLOCKS_ADDED;

    @JsonPath("claims.blacklisted_world")
    public static Component BLACKLISTED_WORLD;

    @JsonPath("claims.set_home_success")
    public static Component SET_HOME_SUCCESS;

    @JsonPath("claims.set_home_fail")
    public static Component SET_HOME_FAIL;

    @JsonPath("claims.reached_members_limit")
    public static String REACHED_MEMBERS_LIMIT;

    @JsonPath("claims.upgrade_success")
    public static String UPGRADE_SUCCESS;

    // PluginFlags

    @JsonPath("flags.default_greeting")
    public static String DEFAULT_GREETING;

    @JsonPath("flags.default_farewell")
    public static String DEFAULT_FAREWELL;

}


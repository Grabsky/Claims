package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginLocale implements JsonConfiguration {

    @JsonPath("missing_permissions")
    public static Component MISSING_PERMISSIONS;

    @JsonPath("reload_success")
    public static Component RELOAD_SUCCESS;

    @JsonPath("reload_failure")
    public static Component RELOAD_FAILURE;

    // ...

    @JsonPath("claims_not_in_claimed_area")
    public static Component NOT_IN_CLAIMED_AREA;

    @JsonPath("claims_not_owner")
    public static Component NOT_CLAIM_OWNER;

    @JsonPath("claim_does_not_exist")
    public static Component CLAIM_DOES_NOT_EXIST;

    @JsonPath("claim_no_center_defined")
    public static Component CLAIM_NO_CENTER_DEFINED;

    // Claims > Commands > Edit

    @JsonPath("commands.claims.claims_edit_failure")
    public static String CLAIMS_EDIT_FAILURE;

    // Claims > Commands > Claims > Get

    @JsonPath("commands.claims.claims_get_success")
    public static Component CLAIMS_GET_SUCCESS;

    @JsonPath("commands.claims.claims_get_failure")
    public static Component CLAIMS_GET_FAILURE;

    // Claims > Commands > Claims > Find

    @JsonPath("commands.claims.claims_find_owner_of")
    public static String CLAIMS_FIND_OWNER_OF;

    @JsonPath("commands.claims.claims_find_member_of")
    public static String CLAIMS_FIND_MEMBER_OF;

    @JsonPath("commands.claims.claims_find_entry")
    public static String CLAIMS_FIND_ENTRY;

    // Claims > Commands > Claims > Restore

    @JsonPath("commands.claims.claims_restore_success")
    public static Component CLAIMS_RESTORE_SUCCESS;

    // Interface > Upgrade

    @JsonPath("interface.upgrade_success")
    public static String UI_UPGRADE_SUCCESS;

    @JsonPath("interface.upgrade_failure_not_upgradeable")
    public static Component UI_UPGRADE_FAILURE_NOT_UPGRADEABLE;

    @JsonPath("interface.upgrade_failure_missing_items")
    public static Component UI_UPGRADE_FAILURE_MISSING_ITEMS;

    @JsonPath("interface.upgrade_icon_upgrade_ready")
    public static Component UPGRADE_ICON_UPGRADE_READY;

    @JsonPath("interface.upgrade_icon_upgrade_not_upgradeable")
    public static Component UPGRADE_ICON_UPGRADE_NOT_UPGRADEABLE;

    @JsonPath("interface.upgrade_icon_upgrade_missing_items")
    public static Component UPGRADE_ICON_UPGRADE_MISSING_ITEMS;

    // Interface > Set Home

    @JsonPath("interface.set_home_success")
    public static Component UI_SET_HOME_SUCCESS;

    @JsonPath("interface.set_home_failure")
    public static Component UI_SET_HOME_FAILURE;

    // Interface > Members > Add

    @JsonPath("interface.members_add_success")
    public static String UI_MEMBERS_ADD_SUCCESS;

    @JsonPath("interface.members_add_failure_already_added")
    public static Component UI_MEMBERS_ADD_FAILURE_ALREADY_ADDED;

    @JsonPath("interface.members_add_failure_reached_limit")
    public static String UI_MEMBERS_ADD_FAILURE_REACHED_LIMIT;

    // Interface > Members > Remove

    @JsonPath("interface.members_remove_success")
    public static String UI_MEMBERS_REMOVE_SUCCESS;

    @JsonPath("interface.members_remove_failure_not_a_member")
    public static Component UI_MEMBERS_REMOVE_FAILURE_NOT_A_MEMBER;

    // Placement > Place

    @JsonPath("placement.place_success")
    public static Component PLACEMENT_PLACE_SUCCESS;

    @JsonPath("placement.place_failure_overlaps")
    public static Component PLACEMENT_PLACE_FAILURE_OVERLAPS;

    @JsonPath("placement.place_failure_blacklisted_world")
    public static Component PLACEMENT_PLACE_FAILURE_BLACKLISTED_WORLD;

    @JsonPath("placement.place_failure_too_close_to_spawn")
    public static Component PLACEMENT_PLACE_FAILURE_TOO_CLOSE_TO_SPAWN;

    @JsonPath("placement.place_failure_reached_claims_limit")
    public static Component PLACEMENT_PLACE_FAILURE_REACHED_CLAIMS_LIMIT;

    @JsonPath("placement.place_failure_invalid_claim_type")
    public static Component PLACEMENT_PLACE_FAILURE_INVALID_CLAIM_TYPE;

    @JsonPath("placement.place_failure_other_claims_must_be_upgraded")
    public static Component PLACEMENT_PLACE_FAILURE_OTHER_CLAIMS_MUST_BE_UPGRADED;

    // Placement > Destroy

    @JsonPath("placement.destroy_success")
    public static Component PLACEMENT_DESTROY_SUCCESS;

    @JsonPath("placement.destroy_failure_not_sneaking")
    public static Component PLACEMENT_DESTROY_FAILURE_NOT_SNEAKING;

    // Flags

    @JsonPath("flags.default_claim_enter")
    public static String FLAGS_CLAIM_ENTER;

    @JsonPath("flags.default_claim_leave")
    public static String FLAGS_CLAIM_LEAVE;

}


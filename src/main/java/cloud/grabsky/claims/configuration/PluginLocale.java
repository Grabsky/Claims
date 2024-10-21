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

import cloud.grabsky.configuration.JsonAdapter;
import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import cloud.grabsky.configuration.paper.adapter.StringComponentAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginLocale implements JsonConfiguration {

    @JsonPath("missing_permissions")
    public static Component MISSING_PERMISSIONS;

    @JsonPath("reload_success")
    public static Component RELOAD_SUCCESS;

    @JsonPath("reload_failure")
    public static Component RELOAD_FAILURE;

    @JsonPath("teleport_in_progress")
    public static String TELEPORT_IN_PROGRESS;

    @JsonPath("teleport_success")
    public static Component TELEPORT_SUCCESS;

    @JsonPath("teleport_failure_moved")
    public static Component TELEPORT_FAILURE_MOVED;

    @JsonPath("teleport_failure_unknown")
    public static Component TELEPORT_FAILURE_UNKNOWN;

    @JsonPath("claims_not_in_claimed_area")
    public static Component NOT_IN_CLAIMED_AREA;

    @JsonPath("claims_not_owner")
    public static Component NOT_CLAIM_OWNER;

    @JsonPath("claims_not_member")
    public static Component NOT_CLAIM_MEMBER;

    @JsonPath("claim_does_not_exist")
    public static Component CLAIM_DOES_NOT_EXIST;

    @JsonPath("claim_no_center_defined")
    public static Component CLAIM_NO_CENTER_DEFINED;

    @JsonPath("random_teleport_searching")
    public static Component RANDOM_TELEPORT_SEARCHING;

    @JsonPath("random_teleport_failure_not_found")
    public static Component RANDOM_TELEPORT_FAILURE_NOT_FOUND;

    @JsonPath("portal_teleport_failure")
    public static Component PORTAL_TELEPORT_FAILURE;

    @JsonPath("claim_border_enabled")
    public static Component CLAIM_BORDER_ENABLED;

    @JsonPath("claim_border_disabled")
    public static Component CLAIM_BORDER_DISABLED;

    @JsonPath("claim_border_shaders_not_supported")
    public static Component CLAIM_BORDER_SHADERS_NOT_SUPPORTED;

    @JsonPath("dimensions")
    public static Map<String, String> DIMENSIONS;


    // Command Arguments

    @JsonPath("command_arguments.argument_claim_not_found")
    public static String ARGUMENT_CLAIM_NOT_FOUND;

    @JsonPath("command_arguments.argument_claim_not_in_claimed_area")
    public static Component ARGUMENT_CLAIM_NOT_IN_CLAIMED_AREA;


    // Commands > Claims

    @JsonPath("commands.claims_usage")
    public static Component COMMAND_CLAIMS_USAGE;

    @JsonPath("commands.claims_edit_failure_already_in_use")
    public static String COMMAND_CLAIMS_EDIT_FAILURE_ALREADY_IN_USE;

    // Commands > Claims > Get

    @JsonPath("commands.claims_get_success")
    public static Component COMMAND_CLAIMS_GET_SUCCESS;

    @JsonPath("commands.claims_get_failure")
    public static Component COMMAND_CLAIMS_GET_FAILURE;

    // Commands > Claims > List

    @JsonPath("commands.claims_list_usage")
    public static String COMMAND_CLAIMS_LIST_USAGE;

    @JsonPath("commands.claims_list_owner_of_header")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_CLAIMS_LIST_OWNER_OF_HEADER;

    @JsonPath("commands.claims_list_owner_of_footer")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_CLAIMS_LIST_OWNER_OF_FOOTER;

    @JsonPath("commands.claims_list_owner_of_none")
    public static String COMMAND_CLAIMS_LIST_OWNER_OF_NONE;

    @JsonPath("commands.claims_list_member_of_header")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_CLAIMS_LIST_MEMBER_OF_HEADER;

    @JsonPath("commands.claims_list_member_of_footer")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_CLAIMS_LIST_MEMBER_OF_FOOTER;

    @JsonPath("commands.claims_list_member_of_none")
    public static String COMMAND_CLAIMS_LIST_MEMBER_OF_NONE;

    @JsonPath("commands.claims_list_entry")
    public static String COMMAND_CLAIMS_LIST_ENTRY;

    // Commands > Claims > Restore

    @JsonPath("commands.claims_restore_success")
    public static Component COMMAND_CLAIMS_RESTORE_SUCCESS;

    // Commands > Waypoints

    @JsonPath("commands.waypoints_usage")
    public static Component COMMAND_WAYPOINTS_USAGE;

    // Commands > Waypoints > Create

    @JsonPath("commands.waypoints_create_usage")
    public static Component COMMAND_WAYPOINTS_CREATE_USAGE;

    @JsonPath("commands.waypoints_create_success")
    public static String COMMAND_WAYPOINTS_CREATE_SUCCESS;

    @JsonPath("commands.waypoints_create_failure_already_exists")
    public static String COMMAND_WAYPOINTS_CREATE_FAILURE_ALREADY_EXISTS;

    // Commands > Waypoints > Remove

    @JsonPath("commands.waypoints_remove_usage")
    public static Component COMMAND_WAYPOINTS_REMOVE_USAGE;

    @JsonPath("commands.waypoints_remove_success")
    public static String COMMAND_WAYPOINTS_REMOVE_SUCCESS;

    @JsonPath("commands.waypoints_remove_failure_not_found")
    public static String COMMAND_WAYPOINTS_REMOVE_FAILURE_NOT_FOUND;

    // Commands > Waypoints > List

    @JsonPath("commands.waypoints_list_usage")
    public static Component COMMAND_WAYPOINTS_LIST_USAGE;

    @JsonPath("commands.waypoints_list_header")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WAYPOINTS_LIST_HEADER;

    @JsonPath("commands.waypoints_list_footer")
    @JsonAdapter(fromJson = StringComponentAdapter.class)
    public static String COMMAND_WAYPOINTS_LIST_FOOTER;

    @JsonPath("commands.waypoints_list_none")
    public static String COMMAND_WAYPOINTS_LIST_NONE;

    @JsonPath("commands.waypoints_list_entry")
    public static String COMMAND_WAYPOINTS_LIST_ENTRY;

    // Commands > Waypoints > Teleport

    @JsonPath("commands.waypoints_teleport_usage")
    public static Component COMMAND_WAYPOINTS_TELEPORT_USAGE;


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

    // Interface > Browse Waypoints

    @JsonPath("interface.waypoint_teleport_failure_not_existent")
    public static String UI_WAYPOINT_TELEPORT_FAILURE_NOT_EXISTENT;

    @JsonPath("interface.waypoint_rename_success")
    public static String UI_WAYPOINT_RENAME_SUCCESS;

    @JsonPath("interface.waypoint_rename_failure_invalid_string")
    public static Component UI_WAYPOINT_RENAME_FAILURE_INVALID_STRING;

    // Interface > Browse Owned Claims

    @JsonPath("interface.claim_rename_success")
    public static String UI_CLAIM_RENAME_SUCCESS;

    @JsonPath("interface.claim_rename_failure_invalid_string")
    public static Component UI_CLAIM_RENAME_FAILURE_INVALID_STRING;

    // Interface > Transfer Waypoint

    @JsonPath("interface.waypoint_transfer_success")
    public static String UI_WAYPOINT_TRANSFER_SUCCESS;

    @JsonPath("interface.waypoint_transfer_success_target")
    public static String UI_WAYPOINT_TRANSFER_SUCCESS_TARGET;

    @JsonPath("interface.waypoint_transfer_failure_reached_limit")
    public static String UI_WAYPOINT_TRANSFER_FAILURE_REACHED_LIMIT;


    // Claim Placement > Place

    @JsonPath("claim_placement.place_success")
    public static Component PLACEMENT_PLACE_SUCCESS;

    @JsonPath("claim_placement.place_failure_overlaps")
    public static Component PLACEMENT_PLACE_FAILURE_OVERLAPS;

    @JsonPath("claim_placement.place_failure_blacklisted_world")
    public static Component PLACEMENT_PLACE_FAILURE_BLACKLISTED_WORLD;

    @JsonPath("claim_placement.place_failure_too_close_to_spawn")
    public static String PLACEMENT_PLACE_FAILURE_TOO_CLOSE_TO_SPAWN;

    @JsonPath("claim_placement.place_failure_reached_claims_limit")
    public static String PLACEMENT_PLACE_FAILURE_REACHED_CLAIMS_LIMIT;

    @JsonPath("claim_placement.place_failure_invalid_claim_type")
    public static Component PLACEMENT_PLACE_FAILURE_INVALID_CLAIM_TYPE;

    @JsonPath("claim_placement.place_failure_other_claims_must_be_upgraded")
    public static Component PLACEMENT_PLACE_FAILURE_OTHER_CLAIMS_MUST_BE_UPGRADED;

    // Claim Placement > Destroy

    @JsonPath("claim_placement.destroy_success")
    public static Component PLACEMENT_DESTROY_SUCCESS;

    @JsonPath("claim_placement.destroy_failure_not_sneaking")
    public static Component PLACEMENT_DESTROY_FAILURE_NOT_SNEAKING;


    // Waypoint Placement

    @JsonPath("waypoint_placement.place_success")
    public static String WAYPOINT_PLACE_SUCCESS;

    @JsonPath("waypoint_placement.place_failure_already_exists")
    public static Component WAYPOINT_PLACE_FAILURE_ALREADY_EXISTS;

    @JsonPath("waypoint_placement.place_failure_reached_waypoints_limit")
    public static String WAYPOINT_PLACE_FAILURE_REACHED_WAYPOINTS_LIMIT;


    // Flags

    @JsonPath("flags.default_claim_enter")
    public static String FLAGS_CLAIM_ENTER;

    @JsonPath("flags.default_claim_leave")
    public static String FLAGS_CLAIM_LEAVE;


    public static final class Commands implements JsonConfiguration {

        // Commands > General

        @JsonPath("missing_permissions")
        public static Component MISSING_PERMISSIONS;

        // Commands > Executors

        @JsonPath("invalid_executor_player")
        public static Component INVALID_EXECUTOR_PLAYER;

        @JsonPath("invalid_executor_console")
        public static Component INVALID_EXECUTOR_CONSOLE;

        // Commands > Arguments

        @JsonPath("invalid_boolean")
        public static String INVALID_BOOLEAN;

        @JsonPath("invalid_short")
        public static String INVALID_SHORT;

        @JsonPath("invalid_short_not_in_range")
        public static String INVALID_SHORT_NOT_IN_RANGE;

        @JsonPath("invalid_integer")
        public static String INVALID_INTEGER;

        @JsonPath("invalid_integer_not_in_range")
        public static String INVALID_INTEGER_NOT_IN_RANGE;

        @JsonPath("invalid_long")
        public static String INVALID_LONG;

        @JsonPath("invalid_long_not_in_range")
        public static String INVALID_LONG_NOT_IN_RANGE;

        @JsonPath("invalid_float")
        public static String INVALID_FLOAT;

        @JsonPath("invalid_float_not_in_range")
        public static String INVALID_FLOAT_NOT_IN_RANGE;

        @JsonPath("invalid_double")
        public static String INVALID_DOUBLE;

        @JsonPath("invalid_double_not_in_range")
        public static String INVALID_DOUBLE_NOT_IN_RANGE;

        @JsonPath("invalid_uuid")
        public static String INVALID_UUID;

        @JsonPath("invalid_player")
        public static String INVALID_PLAYER;

        @JsonPath("invalid_offline_player")
        public static String INVALID_OFFLINE_PLAYER;

        @JsonPath("invalid_world")
        public static String INVALID_WORLD;

        @JsonPath("invalid_enchantment")
        public static String INVALID_ENCHANTMENT;

        @JsonPath("invalid_material")
        public static String INVALID_MATERIAL;

        @JsonPath("invalid_entity_type")
        public static String INVALID_ENTITY_TYPE;

        @JsonPath("invalid_namespacedkey")
        public static String INVALID_NAMESPACEDKEY;

        @JsonPath("invalid_position")
        public static String INVALID_POSITION;

    }

}


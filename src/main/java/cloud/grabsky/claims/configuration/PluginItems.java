package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginItems implements JsonConfiguration {

    @JsonPath("interface.category_homes")
    public static ItemStack UI_CATEGORY_HOMES;

    @JsonPath("interface.category_members")
    public static ItemStack UI_CATEGORY_MEMBERS;

    @JsonPath("interface.category_settings")
    public static ItemStack UI_CATEGORY_SETTINGS;

    @JsonPath("interface.category_flags")
    public static ItemStack UI_CATEGORY_FLAGS;

    @JsonPath("interface.icon_set_teleport")
    public static ItemStack UI_ICON_SET_TELEPORT;

    @JsonPath("interface.icon_browse_players")
    public static ItemStack UI_ICON_BROWSE_PLAYERS;

    @JsonPath("interface.icon_add_member")
    public static ItemStack UI_ICON_ADD_MEMBER;

    @JsonPath("interface.icon_remove_member")
    public static ItemStack UI_ICON_REMOVE_MEMBER;

    @JsonPath("interface.icon_teleport_to_spawn")
    public static ItemStack UI_ICON_TELEPORT_TO_SPAWN;

    @JsonPath("interface.icon_browse_waypoints")
    public static ItemStack UI_ICON_BROWSE_WAYPOINTS;

    @JsonPath("interface.icon_browse_owned_claims")
    public static ItemStack UI_ICON_BROWSE_OWNED_CLAIMS;

    @JsonPath("interface.icon_browse_relative_claims")
    public static ItemStack UI_ICON_BROWSE_RELATIVE_CLAIMS;

    @JsonPath("interface.icon_waypoint_block_source")
    public static ItemStack UI_ICON_WAYPOINT_BLOCK_SOURCE;

    @JsonPath("interface.icon_waypoint_command_source")
    public static ItemStack UI_ICON_WAYPOINT_COMMAND_SOURCE;

    @JsonPath("interface.navigation_previous")
    public static ItemStack UI_NAVIGATION_PREVIOUS;

    @JsonPath("interface.navigation_next")
    public static ItemStack UI_NAVIGATION_NEXT;

    @JsonPath("interface.navigation_return")
    public static ItemStack UI_NAVIGATION_RETURN;

}

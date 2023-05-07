package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginItems implements JsonConfiguration {

    // Interface > Categories > Browse Teleports
    @JsonPath("interface.categories.browse_teleports")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_TELEPORTS;

    // Interface > Categories > Browse Settings
    @JsonPath("interface.categories.browse_settings")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_SETTINGS;

    // Interface > Categories > Browse Flags
    @JsonPath("interface.categories.browse_flags")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_FLAGS;

    // Interface > Categories > Browse Members
    @JsonPath("interface.categories.browse_members")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_MEMBERS;

    // Interface > Categories > Browse Online Players
    @JsonPath("interface.categories.browse_online_players")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_ONLINE_PLAYERS;

    // Interface > Categories > Browse Waypoints
    @JsonPath("interface.categories.browse_waypoints")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_WAYPOINTS;

    // Interface > Categories > Browse Owned Claims
    @JsonPath("interface.categories.browse_owned_claims")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_OWNED_CLAIMS;

    // Interface > Categories > Browse Relative Claims
    @JsonPath("interface.categories.browse_relative_claims")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_RELATIVE_CLAIMS;


    // Interface > Functional > Icon Add Member
    @JsonPath("interface.functional.icon_add_member")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_ADD_MEMBER;

    // Interface > Functional > Icon Remove Member
    @JsonPath("interface.functional.icon_remove_member")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_REMOVE_MEMBER;

    // Interface > Functional > Icon Spawn
    @JsonPath("interface.functional.icon_spawn")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_SPAWN;

    // Interface > Functional > Icon Waypoint (Block)
    @JsonPath("interface.functional.icon_waypoint_block")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_WAYPOINT_BLOCK;

    // Interface > Functional > Icon Waypoint (Command)
    @JsonPath("interface.functional.icon_waypoint_command")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_WAYPOINT_COMMAND;

    // Interface > Functional > Icon Claim (Owned)
    @JsonPath("interface.functional.icon_owned_claim")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_OWNED_CLAIM;

    // Interface > Functional > Icon Claim (Relative)
    //  @JsonPath("interface.functional.icon_relative_claim")
    //  public static ItemStack INTERFACE_FUNCTIONAL_ICON_RELATIVE_CLAIM;


    // Interface > Navigation > Next Page
    @JsonPath("interface.navigation.next_page")
    public static ItemStack INTERFACE_NAVIGATION_NEXT_PAGE;

    // Interface > Navigation > Previous Page
    @JsonPath("interface.navigation.previous_page")
    public static ItemStack INTERFACE_NAVIGATION_PREVIOUS_PAGE;

    // Interface > Navigation > Return
    @JsonPath("interface.navigation.return")
    public static ItemStack INTERFACE_NAVIGATION_RETURN;

}

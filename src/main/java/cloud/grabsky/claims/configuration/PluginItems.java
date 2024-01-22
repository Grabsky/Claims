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

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginItems implements JsonConfiguration {

    // Interface > Categories > Browse Teleports
    @JsonPath("interface.categories.browse_teleports")
    public static ItemStack INTERFACE_CATEGORIES_BROWSE_TELEPORTS;

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
    @JsonPath("interface.functional.icon_set_home")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_SET_HOME;

    // Interface > Functional > Icon Spawn
    @JsonPath("interface.functional.icon_spawn")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_SPAWN;

    // Interface > Functional > Icon Random Teleport
    @JsonPath("interface.functional.icon_random_teleport")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_RANDOM_TELEPORT;

    // Interface > Functional > Icon Waypoint
    @JsonPath("interface.functional.icon_waypoint")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_WAYPOINT;

    // Interface > Functional > Icon Waypoint (Invalid)
    @JsonPath("interface.functional.icon_waypoint_invalid")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_WAYPOINT_INVALID;

    // Interface > Functional > Icon Claim (Owned)
    @JsonPath("interface.functional.icon_owned_claim")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_OWNED_CLAIM;

    // Interface > Functional > Icon Claim (Relative)
    @JsonPath("interface.functional.icon_relative_claim")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_RELATIVE_CLAIM;

    // Interface > Functional > Icon Delete Waypoint
    @JsonPath("interface.functional.icon_delete_waypoint")
    public static ItemStack INTERFACE_FUNCTIONAL_ICON_DELETE_WAYPOINT;


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

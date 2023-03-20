package cloud.grabsky.claims.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PluginItems implements JsonConfiguration {

    @JsonPath("upgrade_crystal")
    public static ItemStack UPGRADE_CRYSTAL;

    @JsonPath("category_homes")
    public static ItemStack CATEGORY_HOMES;

    @JsonPath("category_members")
    public static ItemStack CATEGORY_MEMBERS;

    @JsonPath("category_settings")
    public static ItemStack CATEGORY_SETTINGS;

    @JsonPath("category_flags")
    public static ItemStack CATEGORY_FLAGS;

    @JsonPath("icon_set_teleport")
    public static ItemStack ICON_SET_TELEPORT;

    @JsonPath("icon_browse_players")
    public static ItemStack ICON_BROWSE_PLAYERS;

    @JsonPath("icon_add_member")
    public static ItemStack ICON_ADD_MEMBER;

    @JsonPath("icon_remove_member")
    public static ItemStack ICON_REMOVE_MEMBER;

    @JsonPath("navigation_previous")
    public static ItemStack NAVIGATION_PREVIOUS;

    @JsonPath("navigation_next")
    public static ItemStack NAVIGATION_NEXT;

    @JsonPath("navigation_return")
    public static ItemStack NAVIGATION_RETURN;

}

package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.claims.ClaimFlag;
import cloud.grabsky.claims.panel.ClaimPanel;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public class ViewFlags extends ClaimPanel.View {

    private static final Component INVENTORY_TITLE = text("\u7000\u7104", NamedTextColor.WHITE);

    @Override
    public void accept(final ClaimPanel panel) {
        final ProtectedRegion region = panel.getClaim().getRegion();
        // Changing panel texture
        panel.updateClientTitle(INVENTORY_TITLE);
        // ...ROW 1
        panel.setItem(11,
                createDisplay(region, Flags.USE, PluginFlags.USE),
                createClickAction(region, Flags.USE, PluginFlags.USE, RegionGroup.NON_MEMBERS)
        );
        panel.setItem(12,
                createDisplay(region, Flags.ENTRY, PluginFlags.ENTRY),
                createClickAction(region, Flags.ENTRY, PluginFlags.ENTRY, RegionGroup.NON_MEMBERS));
        panel.setItem(13,
                createDisplay(region, Flags.TNT, PluginFlags.TNT),
                createClickAction(region, Flags.TNT, PluginFlags.TNT));
        panel.setItem(14,
                createDisplay(region, Flags.CREEPER_EXPLOSION, PluginFlags.CREEPER_EXPLOSION),
                createClickAction(region, Flags.CREEPER_EXPLOSION, PluginFlags.CREEPER_EXPLOSION));
        panel.setItem(15,
                createDisplay(region, Flags.SNOW_MELT, PluginFlags.SNOW_MELT),
                createClickAction(region, Flags.SNOW_MELT, PluginFlags.SNOW_MELT));
        // ...ROW 2
        panel.setItem(20,
                createDisplay(region, Flags.ICE_MELT, PluginFlags.ICE_MELT),
                createClickAction(region, Flags.ICE_MELT, PluginFlags.ICE_MELT));
        panel.setItem(21,
                createDisplay(region, Flags.FIRE_SPREAD, PluginFlags.FIRE_SPREAD),
                createClickAction(region, Flags.FIRE_SPREAD, PluginFlags.FIRE_SPREAD));
        panel.setItem(22,
                createDisplay(region, Flags.MOB_SPAWNING, PluginFlags.MOB_SPAWNING),
                createClickAction(region, Flags.MOB_SPAWNING, PluginFlags.MOB_SPAWNING));
        panel.setItem(23,
                createDisplay(region, Claims.CustomFlag.CLIENT_TIME, PluginFlags.CLIENT_TIME),
                createClickAction(region, Claims.CustomFlag.CLIENT_TIME, PluginFlags.CLIENT_TIME));
        panel.setItem(24,
                createDisplay(region, Claims.CustomFlag.CLIENT_WEATHER, PluginFlags.WEATHER_LOCK),
                createClickAction(region, Claims.CustomFlag.CLIENT_WEATHER, PluginFlags.WEATHER_LOCK));
        // ...
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> panel.applyView(new ViewSettings(), true));
   }

   private static <T> ItemStack createDisplay(final ProtectedRegion region, final Flag<T> flag, final ClaimFlag<T> claimFlag) {
        return claimFlag.getDisplay(region.getFlag(flag));
   }

   private static <T> Panel.ClickAction createClickAction(final ProtectedRegion region, final Flag<T> flag, final ClaimFlag<T> claimFlag) {
        return (event) -> {
            final T current = region.getFlag(flag);
            final T next = claimFlag.next(current);
            // ...
            region.setFlag(flag, next);
            // ...
            event.setCurrentItem(claimFlag.getDisplay(next));
        };
   }

    private static <T> Panel.ClickAction createClickAction(final ProtectedRegion region, final Flag<T> flag, final ClaimFlag<T> claimFlag, final RegionGroup regionGroup) {
        return (event) -> {
            final T current = region.getFlag(flag);
            final T next = claimFlag.next(current);
            // ...
            region.setFlag(flag, next);
            region.setFlag(flag.getRegionGroupFlag(), regionGroup);
            // ...
            event.setCurrentItem(claimFlag.getDisplay(next));
        };
    }

}

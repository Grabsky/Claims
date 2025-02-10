/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimFlag;
import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.translatable;

public enum BrowseFlags implements Consumer<ClaimPanel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = translatable("ui.claims.browse_flags", NamedTextColor.WHITE);

    public void DFU(final Claim claim) {
        if (claim.getFlag(Flags.GRASS_SPREAD) == null)
            claim.setFlag(Flags.GRASS_SPREAD, PluginFlags.GRASS_GROWTH.getDefaultValue());
        if (claim.getFlag(Flags.VINE_GROWTH) == null)
            claim.setFlag(Flags.VINE_GROWTH, PluginFlags.VINE_GROWTH.getDefaultValue());
        if (claim.getFlag(Flags.LEAF_DECAY) == null)
            claim.setFlag(Flags.LEAF_DECAY, PluginFlags.LEAF_DECAY.getDefaultValue());
    }

    @Override
    public void accept(final ClaimPanel cPanel) {
        // Returning in case there is no Claim object associated with this ClaimPanel.
        if (cPanel.getClaim() == null) {
            cPanel.close();
            return;
        }
        // ...
        final Claim claim = cPanel.getClaim();
        // Calling DFU for flags that were added after claimed region has been created.
        DFU(claim);
        // Changing (client-side) title of the inventory to render custom resource-pack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // ...ROW 1
        cPanel.setItem(11,
                createDisplay(claim, Flags.USE, PluginFlags.USE),
                createClickAction(claim, Flags.USE, PluginFlags.USE, RegionGroup.NON_MEMBERS)
        );
        cPanel.setItem(12,
                createDisplay(claim, Flags.CHEST_ACCESS, PluginFlags.CHEST_ACCESS),
                createClickAction(claim, Flags.CHEST_ACCESS, PluginFlags.CHEST_ACCESS, RegionGroup.NON_MEMBERS));
        cPanel.setItem(13,
                createDisplay(claim, Flags.TNT, PluginFlags.TNT),
                createClickAction(claim, Flags.TNT, PluginFlags.TNT));
        cPanel.setItem(14,
                createDisplay(claim, Flags.CREEPER_EXPLOSION, PluginFlags.CREEPER_EXPLOSION),
                createClickAction(claim, Flags.CREEPER_EXPLOSION, PluginFlags.CREEPER_EXPLOSION));
        cPanel.setItem(15,
                createDisplay(claim, Flags.SNOW_MELT, PluginFlags.SNOW_MELT),
                createClickAction(claim, Flags.SNOW_MELT, PluginFlags.SNOW_MELT));
        // ...ROW 2
        cPanel.setItem(20,
                createDisplay(claim, Flags.ICE_MELT, PluginFlags.ICE_MELT),
                createClickAction(claim, Flags.ICE_MELT, PluginFlags.ICE_MELT));
        cPanel.setItem(21,
                createDisplay(claim, Flags.FIRE_SPREAD, PluginFlags.FIRE_SPREAD),
                createClickAction(claim, Flags.FIRE_SPREAD, PluginFlags.FIRE_SPREAD));
        cPanel.setItem(22,
                createDisplay(claim, Flags.MOB_SPAWNING, PluginFlags.MOB_SPAWNING),
                createClickAction(claim, Flags.MOB_SPAWNING, PluginFlags.MOB_SPAWNING));
        cPanel.setItem(23,
                createDisplay(claim, Claims.CustomFlag.CLIENT_TIME, PluginFlags.CLIENT_TIME),
                createClickAction(claim, Claims.CustomFlag.CLIENT_TIME, PluginFlags.CLIENT_TIME));
        cPanel.setItem(24,
                createDisplay(claim, Claims.CustomFlag.CLIENT_WEATHER, PluginFlags.CLIENT_WEATHER),
                createClickAction(claim, Claims.CustomFlag.CLIENT_WEATHER, PluginFlags.CLIENT_WEATHER));
        // ...ROW 3
        cPanel.setItem(29,
                createDisplay(claim, Flags.GRASS_SPREAD, PluginFlags.GRASS_GROWTH),
                createClickAction(claim, Flags.GRASS_SPREAD, PluginFlags.GRASS_GROWTH));
        cPanel.setItem(30,
                createDisplay(claim, Flags.VINE_GROWTH, PluginFlags.VINE_GROWTH),
                createClickAction(claim, Flags.VINE_GROWTH, PluginFlags.VINE_GROWTH));
        cPanel.setItem(31,
                createDisplay(claim, Flags.LEAF_DECAY, PluginFlags.LEAF_DECAY),
                createClickAction(claim, Flags.LEAF_DECAY, PluginFlags.LEAF_DECAY));
        // ...
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseCategories.INSTANCE, true));
   }

   private static <T> ItemStack createDisplay(final Claim claim, final Flag<T> flag, final ClaimFlag<T> claimFlag) {
        return claimFlag.getDisplay(claim.getFlag(flag));
   }

   private static <T> Panel.ClickAction createClickAction(final Claim claim, final Flag<T> flag, final ClaimFlag<T> claimFlag) {
        return (event) -> {
            final T current = claim.getFlag(flag);
            final T next = claimFlag.next(current);
            // ...
            claim.setFlag(flag, next);
            // ...
            event.setCurrentItem(claimFlag.getDisplay(next));
        };
   }

    private static <T> Panel.ClickAction createClickAction(final Claim claim, final Flag<T> flag, final ClaimFlag<T> claimFlag, final RegionGroup regionGroup) {
        return (event) -> {
            final T current = claim.getFlag(flag);
            final T next = claimFlag.next(current);
            // ...
            claim.setFlag(flag, next);
            claim.setFlag(flag.getRegionGroupFlag(), regionGroup);
            // ...
            event.setCurrentItem(claimFlag.getDisplay(next));
        };
    }

}

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

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public final class BrowseClaimOnlinePlayers implements Consumer<ClaimPanel> {

    private List<ClaimPlayer> onlineClaimPlayers = new ArrayList<>();

    private static final Component INVENTORY_TITLE = translatable("ui.claims.browse_claim_online_players", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    @Override
    public void accept(final ClaimPanel cPanel) {
        // Returning in case there is no Claim object associated with this ClaimPanel.
        if (cPanel.getClaim() == null) {
            cPanel.close();
            return;
        }
        // ...
        final ClaimManager claimManager = cPanel.getClaim().getManager();
        final Claim claim = cPanel.getClaim();
        // ...
        this.onlineClaimPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player -> cPanel.getViewer().canSee(player) == true)
                .map(claimManager::getClaimPlayer)
                .filter(claimPlayer -> claimPlayer.isOwnerOf(claim) == false) // excluding owner(s)
                .filter(claimPlayer -> claimPlayer.isMemberOf(claim) == false) // excluding member(s)
                .toList();
        // Changing (client-side) title of the inventory to render custom resource-pack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // "Rendering" the inventory contents.
        this.render(cPanel, 1, UI_SLOTS.size());
    }

    private void render(final ClaimPanel cPanel, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final var onlineClaimPlayersIterator = moveIterator(onlineClaimPlayers.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        final var uiSlotsIterator = UI_SLOTS.iterator();
        // ...
        // Rendering PREVIOUS PAGE button.
        if (onlineClaimPlayersIterator.hasPrevious() == true)
            cPanel.setItem(18, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> this.render(cPanel, pageToDisplay - 1, maxOnPage));
        // ...
        final Player viewer = cPanel.getViewer();
        final Claim claim = cPanel.getClaim();
        // ...
        while (onlineClaimPlayersIterator.hasNext() == true && uiSlotsIterator.hasNext() == true) {
            final ClaimPlayer claimPlayer = onlineClaimPlayersIterator.next();
            final User user = claimPlayer.toUser();
            // ...
            final ItemStack head = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_ADD_MEMBER)
                    .setName(text(user.getName(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                    .setSkullTexture(user.getTextures())
                    .build();
            // ...
            cPanel.setItem(uiSlotsIterator.next(), head, (event) -> {
                // Making sure not to exceed the members limit.
                if (claim.getMembers().size() < PluginConfig.CLAIM_SETTINGS_MEMBERS_LIMIT) {
                    // Trying to add player to the claim. This method fails if player is already member of that claim.
                    if (claim.addMember(claimPlayer) == true) {
                        cPanel.applyClaimTemplate(BrowseMembers.INSTANCE, true);
                        return;
                    }
                    // Closing the panel.
                    viewer.closeInventory();
                    // Sending error message that claim members limit has been reached.
                    Message.of(PluginLocale.UI_MEMBERS_ADD_FAILURE_ALREADY_ADDED).send(viewer);
                }
                // Closing the panel.
                viewer.closeInventory();
                // Sending error message that claim members limit has been reached.
                Message.of(PluginLocale.UI_MEMBERS_ADD_FAILURE_REACHED_LIMIT).placeholder("limit", PluginConfig.CLAIM_SETTINGS_MEMBERS_LIMIT).send(viewer);
            });
        }
        // Rendering NEXT PAGE button.
        if (onlineClaimPlayersIterator.hasNext() == true)
            cPanel.setItem(26, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> this.render(cPanel, pageToDisplay + 1, maxOnPage));
        // ...
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseMembers.INSTANCE, true));
    }

}

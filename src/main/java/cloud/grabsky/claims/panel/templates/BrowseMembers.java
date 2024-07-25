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
package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public enum BrowseMembers implements Consumer<ClaimPanel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = translatable("ui.claims.browse_members", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(11, 12, 13, 14, 15, 20, 21, 22, 23, 24);

    @Override
    public void accept(final ClaimPanel cPanel) {
        // Returning in case there is no Claim object associated with this ClaimPanel.
        if (cPanel.getClaim() == null) {
            cPanel.close();
            return;
        }
        // Changing (client-side) title of the inventory to render custom resource-pack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // "Rendering" the inventory contents.
        this.render(cPanel);
    }

    private void render(final ClaimPanel cPanel) {
        cPanel.clear();
        // ...
        final Player viewer = cPanel.getViewer();
        // For each added member slot
        final var slotsIterator = UI_SLOTS.iterator();
        final var membersIterator = cPanel.getClaim().getMembers().iterator();
        // ...
        while (membersIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final ClaimPlayer member = membersIterator.next();
            final User user = member.toUser();
            // ...
            final ItemStack head = (user == null)
                    ? new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_REMOVE_MEMBER)
                            .setName(text(member.getUniqueId().toString(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                            .build()
                    : new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_REMOVE_MEMBER)
                            .setName(text(user.getName(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                            .setSkullTexture(user.getTextures())
                            .build();
            // ...
            cPanel.setItem(slotsIterator.next(), head, (event) -> {
                // One more check just in case something changed while GUI was open
                if (cPanel.getClaim().removeMember(member) == true) {
                    this.render(cPanel);
                    return;
                }
                cPanel.close();
                Message.of(PluginLocale.UI_MEMBERS_REMOVE_FAILURE_NOT_A_MEMBER).send(viewer);
            });
        }
        // Displaying [ICON_BROWSE_PLAYERS] button.
        if (slotsIterator.hasNext() == true)
            cPanel.setItem(slotsIterator.next(), PluginItems.INTERFACE_CATEGORIES_BROWSE_ONLINE_PLAYERS, event -> cPanel.applyClaimTemplate(new BrowseClaimOnlinePlayers(), true));
        // return
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseCategories.INSTANCE, true));
    }
}

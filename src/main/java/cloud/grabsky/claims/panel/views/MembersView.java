package cloud.grabsky.claims.panel.views;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
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

public enum MembersView implements Consumer<Panel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = text("\u7000\u7104", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(11, 12, 13, 14, 15,  21, 22, 23, 24, 25);

    @Override
    public void accept(final Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // Changing (client-side) title of the inventory to render custom resourcepack texture on top of it.
        cPanel.updateClientTitle(INVENTORY_TITLE);
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
                    ? new ItemBuilder(PluginItems.UI_ICON_REMOVE_MEMBER)
                            .setName(text(member.getUniqueId().toString(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                            .build()
                    : new ItemBuilder(PluginItems.UI_ICON_REMOVE_MEMBER)
                            .setName(text(user.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
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
            cPanel.setItem(slotsIterator.next(), PluginItems.UI_ICON_BROWSE_PLAYERS, event -> cPanel.applyTemplate(new MembersAddView(), true));
        // return
        cPanel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> cPanel.applyTemplate(MainView.INSTANCE, true));
    }
}

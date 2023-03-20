package cloud.grabsky.claims.panel.views;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static net.kyori.adventure.text.Component.text;

public class ViewMembers extends ClaimPanel.View {

    private final UserCache userCache = AzureProvider.getAPI().getUserCache();

    private static final Component INVENTORY_TITLE = text("\u7000\u7104", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(11, 12, 13, 14, 15,  21, 22, 23, 24, 25);

    @Override
    public void accept(final ClaimPanel panel) {
        // Changing panel texture
        panel.updateClientTitle(INVENTORY_TITLE);
        // Generating the view
        this.generateView(panel);
    }

    private void generateView(final ClaimPanel panel) {
        panel.clear();
        // For each added member slot
        final var slotsIterator = UI_SLOTS.iterator();
        final var membersIterator = panel.getClaim().getMembers().iterator();
        // ...
        while (membersIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final ClaimPlayer member = membersIterator.next();
            final User user = member.toUser();
            // ...
            final ItemStack head = new ItemBuilder(PluginItems.ICON_REMOVE_MEMBER)
                    .setName(text(user.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                    .setSkullTexture(user.getTextures())
                    .build();
            // ...
            panel.setItem(slotsIterator.next(), head, (event) -> {
                // One more check just in case something changed while GUI was open
                if (panel.getClaim().removeMember(member) == true) {
                    this.generateView(panel);
                    return;
                }
                panel.getViewer().closeInventory();
                sendMessage(panel.getViewer(), PluginLocale.NOT_MEMBER);
            });
        }
        // '+' icon
        if (slotsIterator.hasNext() == true)
            panel.setItem(slotsIterator.next(), PluginItems.ICON_BROWSE_PLAYERS, event -> panel.applyView(new ViewMembersAdd(), true));
        // return
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> panel.applyView(new ViewMain(), true));
    }
}

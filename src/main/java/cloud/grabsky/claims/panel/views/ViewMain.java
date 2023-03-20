package cloud.grabsky.claims.panel.views;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.azure.api.user.UserCache;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;

public class ViewMain extends ClaimPanel.View {

    private final UserCache userCache = AzureProvider.getAPI().getUserCache();

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    @Override
    public void accept(final ClaimPanel panel) {
        final Player viewer = panel.getViewer();
        final ClaimPlayer owner = panel.getClaim().getOwner();
        // Changing panel texture
        panel.updateClientTitle(INVENTORY_TITLE);
        // Setting menu items
        panel.setItem(11, PluginItems.CATEGORY_HOMES, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    viewer.closeInventory();
                    PaperLib.teleportAsync(viewer, owner.getClaim().getHome());
                }
                case RIGHT, SHIFT_RIGHT -> panel.applyView(new ViewHomes(), true);
            }
        });
        panel.setItem(13, new ItemBuilder(PluginItems.CATEGORY_MEMBERS).setSkullTexture(owner.toPlayer()).build(), (event) -> panel.applyView(new ViewMembers(), true));
        panel.setItem(15, PluginItems.CATEGORY_SETTINGS, (event) -> panel.applyView(new ViewSettings(), true));
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }

}

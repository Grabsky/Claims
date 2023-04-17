package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public enum MainView implements Consumer<Panel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    @Override
    public void accept(final Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // ...
        final Player viewer = cPanel.getViewer();
        // Changing (client-side) title of the inventory to render custom resourcepack texture on top of it.
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // Setting menu items
        cPanel.setItem(11, PluginItems.UI_CATEGORY_HOMES, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    cPanel.applyTemplate(TeleportView.INSTANCE, true);
                    // viewer.teleportAsync(cPanel.getClaim().getHome());
                }
            }
        });
        cPanel.setItem(13, new ItemBuilder(PluginItems.UI_CATEGORY_MEMBERS).setSkullTexture(viewer).build(), (event) -> cPanel.applyTemplate(MembersView.INSTANCE, true));
        cPanel.setItem(15, PluginItems.UI_CATEGORY_SETTINGS, (event) -> cPanel.applyTemplate(SettingsView.INSTANCE, true));
        cPanel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }

}

package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.util.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public enum BrowseCategories implements Consumer<ClaimPanel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    @Override
    public void accept(final ClaimPanel cPanel) {
        // Returning in case there is no Claim object associated with this ClaimPanel.
        if (cPanel.getClaim() == null) { cPanel.close(); return; }
        // ...
        final Player viewer = cPanel.getViewer();
        // Changing (client-side) title of the inventory to render custom resourcepack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // Setting menu items
        cPanel.setItem(11, PluginItems.INTERFACE_CATEGORIES_BROWSE_TELEPORTS, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    // Closing the panel.
                    cPanel.close();
                    // Teleporting...
                    Utilities.teleport(viewer, cPanel.getClaim().getHome(), PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                        if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                            // Displaying particles. NOTE: This can expose vanished players.
                            if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS != null) {
                                PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                                    viewer.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                });
                            }
                            // Playing sounds. NOTE: This can expose vanished players.
                            if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                                old.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                            if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN != null)
                                current.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
                        }
                    });
                }
                case RIGHT, SHIFT_RIGHT -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true);
            }
        });
        cPanel.setItem(13, new ItemBuilder(PluginItems.INTERFACE_CATEGORIES_BROWSE_MEMBERS).setSkullTexture(viewer).build(), (event) -> cPanel.applyClaimTemplate(BrowseMembers.INSTANCE, true));
        cPanel.setItem(15, PluginItems.INTERFACE_CATEGORIES_BROWSE_SETTINGS, (event) -> cPanel.applyClaimTemplate(BrowseSettings.INSTANCE, true));
        // ...
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }

}

package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.bedrock.helpers.Inventories.hasSimilarItems;
import static cloud.grabsky.bedrock.helpers.Inventories.removeSimilarItems;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

public enum SettingsView implements Consumer<Panel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    @Override
    public void accept(final Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // Changing (client-side) title of the inventory to render custom resourcepack texture on top of it.
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // "Rendering" the inventory contents.
        this.render(cPanel);
    }

    private void render(final ClaimPanel cPanel) {
        final Player viewer = cPanel.getViewer();
        final Claim claim = cPanel.getClaim();
        // ...
        cPanel.clear();
        // Button: FLAGS
        cPanel.setItem(11, PluginItems.UI_CATEGORY_FLAGS, event -> cPanel.applyTemplate(FlagsView.INSTANCE, true));
        // Teleport location button
        cPanel.setItem(13, PluginItems.UI_ICON_SET_TELEPORT, (event) -> {
            viewer.closeInventory();
            // ...
            Message.of(claim.setHome(viewer.getLocation()) == true
                    ? PluginLocale.UI_SET_HOME_SUCCESS
                    : PluginLocale.UI_SET_HOME_FAILURE
            ).send(viewer);
        });
        // Getting object of CURRENT upgrade level
        final Claim.Type type = claim.getType();
        // ...
        final ItemStack upgradeButton = new ItemStack(type.getUpgradeButton());
        // ...
        setUpgradeStatus(upgradeButton, viewer, type);
        // ...
        cPanel.setItem(15, upgradeButton, (event) -> {
            if (type.isUpgradeable() == false)
                return;
            // ...
            if (viewer.hasPermission("claims.bypass.upgrade_cost") == true || hasUpgradeCost(viewer, type) == true) {
                // Removing upgrade cost from Player unless it has bypass permission
                if (viewer.hasPermission("claims.bypass.upgrade_cost") == false)
                    removeSimilarItems(viewer, type.getUpgradeCost());
                // Trying to upgrade the claim...
                if (cPanel.getManager().upgradeClaim(claim) == true) {
                    if (PluginConfig.UI_UPGRADE_SOUND != null)
                        viewer.playSound(PluginConfig.UI_UPGRADE_SOUND);
                    // ...
                    Message.of(PluginLocale.UI_UPGRADE_SUCCESS)
                            .placeholder("size", claim.getType().getRadius() * 2 + 1)
                            .send(viewer);
                    // ...
                    this.render(cPanel);
                    return;
                }
                Message.of(PluginLocale.UI_UPGRADE_FAILURE_MISSING_ITEMS).send(viewer);
            }
        });
        // Return button
        cPanel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> cPanel.applyTemplate(MainView.INSTANCE, true));
    }

    private static void setUpgradeStatus(final @NotNull ItemStack item, final @NotNull Player player, final @NotNull Claim.Type type) {
        final Component statusComponent = (type.isUpgradeable() == true)
                ? (player.hasPermission("claims.bypass.upgrade_cost") == true || hasUpgradeCost(player, type) == true)
                        ? PluginLocale.UPGRADE_ICON_UPGRADE_READY
                        : PluginLocale.UPGRADE_ICON_UPGRADE_MISSING_ITEMS
                : PluginLocale.UPGRADE_ICON_UPGRADE_NOT_UPGRADEABLE;
        // ...
        item.editMeta(meta -> {
            final List<Component> lore = new ArrayList<>();
            // ...
            requirePresent(item.getItemMeta().lore(), new ArrayList<Component>()).forEach(line -> {
                lore.add((plainText().serialize(line).equals("[UPGRADE_STATUS]") == true) ? empty().decoration(TextDecoration.ITALIC, false).append(statusComponent) : line);
            });
            // ...
            meta.lore(lore);
        });
    }

    @Experimental // Inheriting @Experimental status from Inventories#hasSimilarItems
    private static boolean hasUpgradeCost(final @NotNull Player player, final @NotNull Claim.Type type) {
        if (type.getUpgradeCost() == null)
            return false;
        // ...
        return hasSimilarItems(player, type.getUpgradeCost());
    }

}
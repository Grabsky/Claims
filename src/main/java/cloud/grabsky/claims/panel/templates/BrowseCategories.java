package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.bedrock.helpers.Inventories.hasSimilarItems;
import static cloud.grabsky.bedrock.helpers.Inventories.removeSimilarItems;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

public enum BrowseCategories implements Consumer<ClaimPanel> {
    /* SINGLETON */ INSTANCE;

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    private static final String PERMISSION_BYPASS_UPGRADE_COST = "claims.bypass.ignore_upgrade_cost";

    @Override
    public void accept(final ClaimPanel cPanel) {
        // Returning in case there is no Claim object associated with this ClaimPanel.
        if (cPanel.getClaim() == null) { cPanel.close(); return; }
        // Changing (client-side) title of the inventory to render custom resourcepack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // "Rendering" the inventory contents.
        this.render(cPanel.getClaim(), cPanel);
    }

    private void render(final Claim claim, final ClaimPanel cPanel) {
        cPanel.clear();
        // ...
        final Player viewer = cPanel.getViewer();
        // Setting menu items
        cPanel.setItem(11, PluginItems.INTERFACE_CATEGORIES_BROWSE_TELEPORTS, (event) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
        cPanel.setItem(13, new ItemBuilder(PluginItems.INTERFACE_CATEGORIES_BROWSE_MEMBERS).setSkullTexture(viewer).build(), (event) -> cPanel.applyClaimTemplate(BrowseMembers.INSTANCE, true));
        // Button: FLAGS
        cPanel.setItem(15, PluginItems.INTERFACE_CATEGORIES_BROWSE_FLAGS, event -> cPanel.applyClaimTemplate(BrowseFlags.INSTANCE, true));
        // Button: SET HOME
        cPanel.setItem(30, PluginItems.INTERFACE_FUNCTIONAL_ICON_SET_HOME, (event) -> {
            cPanel.close();
            // Getting player's location.
            final Location location = viewer.getLocation();
            // Setting home and sending a message dependent on result.
            Message.of(cPanel.getClaim().setHome(location) == true ? PluginLocale.UI_SET_HOME_SUCCESS : PluginLocale.UI_SET_HOME_FAILURE).send(viewer);
        });
        // Button: UPGRADE
        final Claim.Type type = claim.getType();
        // Getting claim upgrade button.
        final ItemStack upgradeButton = type.getUpgradeButton().clone();
        // Edu
        setUpgradeStatus(upgradeButton, viewer, type);
        // ...
        cPanel.setItem(32, upgradeButton, (event) -> {
            // Returning if claim is already on the max level.
            if (type.isUpgradeable() == false)
                return;
            // ...
            if (viewer.hasPermission(PERMISSION_BYPASS_UPGRADE_COST) == true || hasUpgradeCost(viewer, type) == true) {
                // Removing upgrade cost from Player unless it has bypass permission
                if (viewer.hasPermission(PERMISSION_BYPASS_UPGRADE_COST) == false)
                    removeSimilarItems(viewer, type.getUpgradeCost());
                // Trying to upgrade the claim...
                if (cPanel.getManager().upgradeClaim(claim) == true) {
                    if (PluginConfig.CLAIMS_SETTINGS_UI_UPGRADE_SOUND != null)
                        viewer.playSound(PluginConfig.CLAIMS_SETTINGS_UI_UPGRADE_SOUND);
                    // ...
                    Message.of(PluginLocale.UI_UPGRADE_SUCCESS)
                            .placeholder("size", claim.getType().getRadius() * 2 + 1)
                            .send(viewer);
                    // ...
                    this.render(claim, cPanel);
                    return;
                }
                Message.of(PluginLocale.UI_UPGRADE_FAILURE_MISSING_ITEMS).send(viewer);
            }
        });

        // ...
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }

    private static void setUpgradeStatus(final @NotNull ItemStack item, final @NotNull Player player, final @NotNull Claim.Type type) {
        final Component statusComponent = (type.isUpgradeable() == true)
                ? (player.hasPermission(PERMISSION_BYPASS_UPGRADE_COST) == true || hasUpgradeCost(player, type) == true)
                ? PluginLocale.UPGRADE_ICON_UPGRADE_READY
                : PluginLocale.UPGRADE_ICON_UPGRADE_MISSING_ITEMS
                : PluginLocale.UPGRADE_ICON_UPGRADE_NOT_UPGRADEABLE;
        // ...
        item.editMeta(meta -> {
            final List<Component> newLore = requirePresent(item.lore(), new ArrayList<Component>()).stream().map(line -> {
                if (plainText().serialize(line).equals("[UPGRADE_STATUS]") == false)
                    return line;
                // ...
                return statusComponent.decoration(TextDecoration.ITALIC, false);
            }).toList();
            // ...
            meta.lore(newLore);
        });
    }

    @SuppressWarnings("UnstableApiUsage") @ApiStatus.Experimental
    // Inheriting @Experimental status from Inventories#hasSimilarItems
    private static boolean hasUpgradeCost(final @NotNull Player player, final @NotNull Claim.Type type) {
        final ItemStack[] upgradeCost = type.getUpgradeCost();
        // Returning 'false' if upgrade cost is empty or not set.
        if (upgradeCost == null || upgradeCost.length == 0)
            return false;
        // ...
        return hasSimilarItems(player, upgradeCost);
    }

}

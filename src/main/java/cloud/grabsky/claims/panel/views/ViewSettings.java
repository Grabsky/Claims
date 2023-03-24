package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.sound.Sound.sound;
import static net.kyori.adventure.text.Component.text;

public class ViewSettings implements Consumer<Panel> {

    private static final Component INVENTORY_TITLE = text("\u7000\u7101", NamedTextColor.WHITE);

    private static final Component UPGRADE_READY = text("Kliknij, aby ulepszyć.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    private static final Component UPGRADE_NOT_READY = text("Nie posiadasz wymaganych przedmiotów.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    private static final Component MAX_LEVEL = text("Osiągnąłeś maksymalny poziom terenu.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);

    @Override
    public void accept(final Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // Changing panel texture
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // Setting menu items
        this.generate(cPanel);
    }

    private void generate(final ClaimPanel cPanel) {
        final Player viewer = cPanel.getViewer();
        final Claim claim = cPanel.getClaim();
        // ...
        cPanel.clear();
        // Button: FLAGS
        cPanel.setItem(11, PluginItems.CATEGORY_FLAGS, event -> cPanel.applyTemplate(new ViewFlags(), true));
        // Teleport location button
        cPanel.setItem(13, PluginItems.ICON_SET_TELEPORT, (event) -> {
            sendMessage(viewer, (claim.setHome(viewer.getLocation()))
                    ? PluginLocale.UI_SET_HOME_SUCCESS
                    : PluginLocale.UI_SET_HOME_FAILURE
            );
            viewer.closeInventory();
        });
        // Getting object of CURRENT upgrade level
        final Claim.Type type = claim.getType();
        // ...
        final ItemBuilder upgradeButtonBuilder = new ItemBuilder(type.getUpgradeButton());
        // ...
        upgradeButtonBuilder.addLore(
                (type.isUpgradeable() == true)
                        ? (canUpgrade(viewer, type.getNextType()) == true)
                                ? UPGRADE_READY
                                : UPGRADE_NOT_READY
                        : MAX_LEVEL
        );
        cPanel.setItem(15, upgradeButtonBuilder.build(), (event) -> {
            if (cPanel.getManager().upgradeClaim(claim) == true) {
                final int size = (type.getNextType().getRadius() * 2) + 1;
                viewer.playSound(sound(key("minecraft:entity.player.levelup"), net.kyori.adventure.sound.Sound.Source.MASTER, 1.0F, 1.0F));
                sendMessage(viewer, PluginLocale.UI_UPGRADE_SUCCESS, Placeholder.unparsed("size", size + "x" + size));
                // ...
                this.generate(cPanel);
            }
        });
        // Return button
        cPanel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> cPanel.applyTemplate(new ViewMain(), true));
    }

    private static boolean canUpgrade(final Player player, final Claim.Type type) {
        return player.hasPermission("claims.bypass.upgradecost");
    }

}

package cloud.grabsky.claims.panel.views;

import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static net.kyori.adventure.sound.Sound.sound;
import static net.kyori.adventure.text.Component.text;

public class ViewSettings extends ClaimPanel.View {

    private static final Component UPGRADE_READY = text("Kliknij, aby ulepszyć.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    private static final Component UPGRADE_NOT_READY = text("Nie posiadasz wymaganych przedmiotów.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    private static final Component MAX_LEVEL = text("Osiągnąłeś maksymalny poziom terenu.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);

    @Override
    public void accept(final ClaimPanel panel) {
        // Changing panel texture
        panel.updateClientTitle(text("\u7000\u7101", NamedTextColor.WHITE));
        // Setting menu items
        this.generateView(panel);
    }

    private void generateView(final ClaimPanel panel) {
        // ...
        final Player viewer = panel.getViewer();
        final Claim claim = panel.getClaim();
        // ...
        panel.clear();
        viewer.updateInventory();
        // Button: FLAGS
        panel.setItem(11, PluginItems.CATEGORY_FLAGS, event -> panel.applyView(new ViewFlags(), true));
        // Teleport location button
        panel.setItem(13, PluginItems.ICON_SET_TELEPORT, (event) -> {
            if (claim.setHome(viewer.getLocation())) {
                sendMessage(viewer, PluginLocale.SET_HOME_SUCCESS);
            } else {
                sendMessage(viewer, PluginLocale.SET_HOME_FAIL);
            }
            viewer.closeInventory();
        });
        // Getting object of CURRENT upgrade level
        final Claim.Type type = claim.getType();
        // ...
        final ItemStack upgradeButton = new ItemStack(type.getUpgradeButton());
        // ...
        upgradeButton.editMeta(meta -> {
            final List<Component> lore = (meta.lore() != null) ? meta.lore() : new ArrayList<>();
            // ...
            if (type.isUpgradeable() == true)
                lore.add((canUpgrade(viewer, type.getNextType()) == true) ? UPGRADE_READY : UPGRADE_NOT_READY);
            else
                lore.add(MAX_LEVEL);
            // ...
            meta.lore(lore);
        });
        // ...
        panel.setItem(15, upgradeButton, (event) -> {
            // ...
            claim.getManager().upgradeClaim(claim);
            // ...then
            viewer.playSound(sound(Sound.ENTITY_PLAYER_LEVELUP.getKey(), net.kyori.adventure.sound.Sound.Source.MASTER, 1.0F, 1.0F));
            // ...
            this.generateView(panel);
        });
        // Return button
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> panel.applyView(new ViewMain(), true));
    }

    private static boolean canUpgrade(final Player player, final Claim.Type type) {
        return player.hasPermission("claims.bypass.upgradecost");
    }

}

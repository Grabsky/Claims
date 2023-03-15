package cloud.grabsky.claims.panel.sections;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimLevel;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLocale;
import cloud.grabsky.claims.panel.Panel;
import cloud.grabsky.claims.templates.Icons;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.utils.Components;
import me.grabsky.indigo.utils.Inventories;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SectionSettings extends Section {
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();
    private final Player viewer;
    private final Claim claim;

    private static final Component CLICK_TO_UPGRADE = Components.parseSection("§7Kliknij, aby ulepszyć.").decoration(TextDecoration.ITALIC, false);
    private static final Component CONDITIONS_NOT_MET = Components.parseSection("§cNie posiadasz wymaganych przedmiotów.").decoration(TextDecoration.ITALIC, false);

    public SectionSettings(Panel panel) {
        super(panel);
        this.viewer = panel.getViewer();
        this.claim = panel.getClaimOwner().getClaim();
    }

    @Override
    public void prepare() {
        // Nothing to prepare
    }

    @Override
    public void apply() {
        // Changing panel texture
        panel.updateClientTitle("§f\u7000\u7101");
        // Setting menu items
        this.generateView();
    }

    private boolean canUpgrade(Player player, ClaimLevel level) {
        return viewer.hasPermission("claims.bypass.upgradecost") || Inventories.hasItems(player, level.getUpgradeCost().toArray(new ItemStack[0]));
    }

    private void generateView() {
        panel.clear();
        // Button: FLAGS
        panel.setItem(11, Icons.CATEGORY_FLAGS, event -> panel.applySection(new SectionFlags(panel)));
        // Teleport location button
        panel.setItem(13, Icons.ICON_SET_TELEPORT, (event) -> {
            if (claim.setHome(viewer.getLocation())) {
                ClaimsLocale.send(viewer, ClaimsLocale.SET_HOME_SUCCESS);
            } else {
                ClaimsLocale.send(viewer, ClaimsLocale.SET_HOME_FAIL);
            }
            viewer.closeInventory();
        });
        // Getting object of CURRENT upgrade level
        final ClaimLevel currentLevel = ClaimLevel.getClaimLevel(claim.getLevel());
        // Getting object of NEXT upgrade level; Ignoring for levels higher than 5
        final ClaimLevel nextLevel = (claim.getLevel() < 6) ? ClaimLevel.getClaimLevel(claim.getLevel() + 1) : null;
        // Creating inventory icon
        final ItemStack inventoryIcon = new ItemStack(currentLevel.getInventoryIcon());
        if (nextLevel != null) {
            inventoryIcon.editMeta((meta) -> {
               final List<Component> lore = (meta.hasLore()) ? meta.lore() : new ArrayList<>();
               lore.add(this.canUpgrade(viewer, nextLevel) ? CLICK_TO_UPGRADE : CONDITIONS_NOT_MET);
               meta.lore(lore);
            });
        }
        // Upgrade button
        panel.setItem(15, inventoryIcon, (event) -> {
            if (nextLevel == null) return;
            // Removing material if player doesn't have bypass permission
            if (this.canUpgrade(viewer, nextLevel)) {
                if (!viewer.hasPermission("claims.bypass.upgradecost")) {
                    Inventories.removeItems(viewer, nextLevel.getUpgradeCost().toArray(new ItemStack[0]));
                }
                // Upgrading claim
                claim.upgrade();
                // TO-DO: Use Kyori components
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                // Refreshing the view
                this.generateView();
                if (ClaimsConfig.LOGS) {
                    fileLogger.log(ClaimsConfig.LOG_FORMAT_UPGRADED
                            .replace("{claim-id}", claim.getId())
                            .replace("{claim-level}", String.valueOf(claim.getLevel()))
                            .replace("{issuer-name}", viewer.getName())
                            .replace("{issuer-uuid}", viewer.getUniqueId().toString()));
                }
                return;
            }
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        });
        // Return button
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionMain(panel)));
    }
}

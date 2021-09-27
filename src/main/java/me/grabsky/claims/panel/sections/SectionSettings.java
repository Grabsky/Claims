package me.grabsky.claims.panel.sections;

import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimLevel;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Icons;
import me.grabsky.claims.utils.ClaimsUtils;
import me.grabsky.claims.utils.InventoryUtils;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.utils.Inventories;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class SectionSettings extends Section {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();

    public SectionSettings(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        // Nothing to prepare
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(executor, "§f\u7000\u7101", editMode);
        // Setting menu items
        this.generateView();
    }

    private boolean canUpgrade(Player player, ClaimLevel level) {
        return executor.hasPermission("claims.bypass.upgradecost") || Inventories.hasItems(player, level.getUpgradeCost().toArray(new ItemStack[0]));
    }

    private void generateView() {
        panel.clear();
        // Button: FLAGS
        panel.setItem(11, Icons.CATEGORY_FLAGS, event -> panel.applySection(new SectionFlags(panel, executor, owner, claim)));
        // Teleport location button
        panel.setItem(13, Icons.ICON_SET_TELEPORT, (event) -> {
            if (claim.setHome(executor.getLocation())) {
                ClaimsLang.send(executor, ClaimsLang.SET_HOME_SUCCESS);
            } else {
                ClaimsLang.send(executor, ClaimsLang.SET_HOME_FAIL);
            }
            executor.closeInventory();
        });
        // Getting object of CURRENT upgrade level
        final ClaimLevel currentLevel = ClaimsUtils.getClaimLevel(claim.getLevel());
        // Getting object of NEXT upgrade level
        final ClaimLevel nextLevel = (claim.getLevel() < 5) ? ClaimsUtils.getClaimLevel(claim.getLevel() + 1) : null;
        final ItemStack icon = currentLevel.getIcon().clone();
        if (nextLevel != null) {
            icon.editMeta((meta) -> {
               final List<String> lore = meta.getLore();
               lore.add(this.canUpgrade(executor, nextLevel) ? "§7Kliknij, aby ulepszyć." : "§cNie posiadasz wymaganych przedmiotów.");
               meta.setLore(lore);
            });
        }
        // Upgrade button
        panel.setItem(15, icon, event -> {
            if (nextLevel == null) return;
            // Removing material if player doesn't have bypass permission
            if (this.canUpgrade(executor, nextLevel)) {
                if (!executor.hasPermission("claims.bypass.upgradecost")) {
                    Inventories.removeItems(executor, nextLevel.getUpgradeCost().toArray(new ItemStack[0]));
                }
                // Upgrading claim
                manager.upgrade(claim);
                // Sending success message and playing level up sound
                ClaimsLang.send(executor, ClaimsLang.UPGRADE_SUCCESS.replace("{size}", nextLevel.getSize()));
                executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                // Refreshing the view
                this.generateView();
                if (ClaimsConfig.LOGS) {
                    fileLogger.log(ClaimsConfig.LOG_FORMAT_UPGRADED
                            .replace("{claim-id}", claim.getId())
                            .replace("{claim-level}", String.valueOf(claim.getLevel()))
                            .replace("{issuer-name}", executor.getName())
                            .replace("{issuer-uuid}", executor.getUniqueId().toString()));
                }
                return;
            }
            executor.playSound(executor.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
        });
        // Return button
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, claim)));
    }
}

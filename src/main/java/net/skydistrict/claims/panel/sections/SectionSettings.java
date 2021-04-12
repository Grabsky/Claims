package net.skydistrict.claims.panel.sections;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import net.skydistrict.claims.utils.UpgradeH;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.UUID;

public class SectionSettings extends Section {

    public SectionSettings(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
    }

    @Override
    public void prepare() {

    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7102", editMode);
        // Setting menu items
        this.generateView();
    }

    private void generateView() {
        panel.clear();
        // Flags category
        panel.setItem(12, StaticItems.FLAGS, event -> panel.applySection(new SectionFlags(panel, executor, owner, region)));
        // I'm not sure why it's marked as nullable...
        ClaimLevel currentLevel = UpgradeH.getLevelByAlias(region.getTypeOptions().alias);
        // Getting ItemBuilder for specific alias
        ClaimLevel nextLevel = UpgradeH.getNextLevel(currentLevel);
        // If current level is not the last
        ItemBuilder inventoryItem = currentLevel.getInventoryItem();
        if (nextLevel != null) {
            String canUpgradeString = (executor.hasPermission("skydistrict.claims.bypass.upgradecost") || InventoryH.hasMaterial(executor, nextLevel.getUpgradeMaterial(), 64)) ? "§7Kliknij, aby ulepszyć." : "§cNie posiadasz wymaganych przedmiotów.";
            inventoryItem.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevel.getColor() + currentLevel.getAliasTranslated(),
                    "§8› §7Rozmiar: " + currentLevel.getColor() + currentLevel.getColor(),
                    "",
                    "§7Następny poziom: " + nextLevel.getColor() + nextLevel.getAliasTranslated(),
                    "§8› §7Rozmiar: " + nextLevel.getColor() + nextLevel.getSize(),
                    "",
                    "§7Koszt ulepszenia: " + nextLevel.getColor() + "64x " + nextLevel.getAliasTranslated(),
                    "",
                    canUpgradeString
            );
        } else {
            inventoryItem.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevel.getColor() + currentLevel.getAliasTranslated(),
                    "§8› §7Rozmiar: " + currentLevel.getColor() + currentLevel.getSize(),
                    "",
                    "§7Osiągnąłeś najwyższy poziom terenu."
            );
        }

        panel.setItem(14, inventoryItem.build(), event -> {
            if (nextLevel != null) {
                if (executor.hasPermission("skydistrict.claims.bypass.upgradecost") || InventoryH.hasMaterial(executor, nextLevel.getUpgradeMaterial(), 64)) {
                    if (!executor.hasPermission("skydistrict.claims.bypass.upgradecost")) {
                        InventoryH.removeMaterial(executor, nextLevel.getUpgradeMaterial(), 64);
                    }
                    region.setType(ProtectionStones.getProtectBlockFromAlias(nextLevel.getAlias()));
                    this.redefine(region.getWGRegion());
                    executor.sendMessage(MessageFormat.format(Lang.UPGRADE_SUCCESS, nextLevel.getColor() + nextLevel.getSize()));
                    executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    this.generateView();
                }
            }
        });

        // Return button
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, region)));
    }
}

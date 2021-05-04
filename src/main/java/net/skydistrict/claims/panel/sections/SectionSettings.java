package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.ClaimH;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionSettings extends Section {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();

    private ItemStack teleport;

    public SectionSettings(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        this.teleport = new ItemBuilder(Material.WHITE_BED)
                .setName("§e§lTeleport")
                .setLore("§7Ustaw teleport na teren", "§7na miejsce, w którym stoisz")
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7101", editMode);
        // Setting menu items
        this.generateView();
    }

    private boolean canUpgrade(Player player, Material upgradeMaterial) {
        return executor.hasPermission("skydistrict.claims.bypass.upgradecost") || InventoryH.hasMaterial(executor, upgradeMaterial, 64);
    }

    private void generateView() {
        panel.clear();
        // Flags category
        panel.setItem(11, Items.FLAGS, event -> panel.applySection(new SectionFlags(panel, executor, owner, claim)));
        // Home
        panel.setItem(13, teleport, (event) -> {
            claim.setHome(executor.getLocation());
            event.getCurrentItem().setType(Material.RED_BED);
        });
        final ClaimLevel currentLevel = ClaimH.getClaimLevel(claim.getLevel());
        // Getting ItemBuilder for specific alias
        final ClaimLevel nextLevel = (claim.getLevel() < 4) ? ClaimH.getClaimLevel(claim.getLevel() + 1) : null;
        // If current level is not the last
        final ItemBuilder inventoryItem = currentLevel.getIcon();
        if (nextLevel != null) {
            String canUpgradeString = canUpgrade(executor, nextLevel.getUpgradeMaterial()) ? "§7Kliknij, aby ulepszyć." : "§cNie posiadasz wymaganych przedmiotów.";
            inventoryItem.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevel.getAlias(),
                    "§8› §7Rozmiar: " + currentLevel.getSize(),
                    "",
                    "§7Następny poziom: " +  nextLevel.getAlias(),
                    "§8› §7Rozmiar: " + nextLevel.getSize(),
                    "",
                    "§7Koszt ulepszenia: " + nextLevel.getColor() + "64x " + nextLevel.getAlias(),
                    "",
                    canUpgradeString
            );
        } else {
            inventoryItem.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevel.getAlias(),
                    "§8› §7Rozmiar: " + currentLevel.getSize(),
                    "",
                    "§7Osiągnąłeś najwyższy poziom terenu."
            );
        }
        panel.setItem(15, inventoryItem.build(), event -> {
            if (nextLevel == null) return;
            if (!canUpgrade(executor, nextLevel.getUpgradeMaterial())) return;
            // Removing material if player doesn't have bypass permission
            if (!executor.hasPermission("skydistrict.claims.bypass.upgradecost")) {
                InventoryH.removeMaterial(executor, nextLevel.getUpgradeMaterial(), 64);
            }
            // Upgrading claim
            manager.upgrade(claim);
            // Sending success message and play level up sound
            Lang.send(executor, Lang.UPGRADE_SUCCESS, nextLevel.getSize());
            executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            // Refreshing the view
            this.generateView();
            if (Config.LOGS) {
                fileLogger.log(new StringBuilder().append("CLAIM_UPGRADED | ")
                        .append(claim.getId()).append(" | ")
                        .append(executor.getName()).append(" (").append(executor.getUniqueId()).append(") | ")
                        .append(UserCache.get(owner).getName()).append(" (").append(owner).append(")")
                        .toString());
            }
        });
        // Return button
        panel.setItem(49, Items.RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, claim)));
    }
}

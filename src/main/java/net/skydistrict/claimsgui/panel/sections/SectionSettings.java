package net.skydistrict.claimsgui.panel.sections;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.Lang;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import net.skydistrict.claimsgui.utils.Upgrade;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionSettings extends Section {
    private String nextLevelAlias;
    private ItemStack upgradePrice;
    private ItemStack upgradeItem;
    private boolean canUpgrade;

    public SectionSettings(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
    }

    @Override
    public void prepare() {
        // I'm not sure why it's marked as nullable...
        String currentLevelAlias = (region.getTypeOptions() != null) ? region.getTypeOptions().alias : "";
        ChatColor currentLevelColor = Upgrade.color(currentLevelAlias);
        // Size of current level in readable format
        int currentLevelSizeCalc = Upgrade.getSize(currentLevelAlias) * 2 + 1;
        String currentLevelSize = currentLevelSizeCalc + "x" + currentLevelSizeCalc;
        // Getting ItemBuilder for specific alias
        ItemBuilder builder = Upgrade.getBuilder(currentLevelAlias);
        // If current level is not the last
        if (!currentLevelAlias.equals("EMERALD")) {
            // What's the next level?
            this.nextLevelAlias = Upgrade.getNextLevelAlias(currentLevelAlias);
            // Next level color
            ChatColor nextLevelColor = Upgrade.color(nextLevelAlias);
            // Size of next level in readable format
            int nextSizeCalc = Upgrade.getSize(nextLevelAlias) * 2 + 1;
            String nextSize = nextSizeCalc + "x" + nextSizeCalc;
            // Upgrade price
            this.upgradePrice = Upgrade.getUpgradePrice(nextLevelAlias);
            // Can you upgrade?
            this.canUpgrade = executor.hasPermission("skydistrict.claims.bypass.upgradecost") || executor.getInventory().contains(upgradePrice);
            String canUpgradeString = canUpgrade ? "§7Kliknij, aby ulepszyć." : "§cNie posiadasz wymaganych przedmiotów.";
            // ItemStack
            this.upgradeItem = builder.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevelColor + Upgrade.translate(currentLevelAlias),
                    "§8› §7Rozmiar: " + currentLevelColor + currentLevelSize,
                    "",
                    "§7Następny poziom: " + nextLevelColor + Upgrade.translate(nextLevelAlias),
                    "§8› §7Rozmiar: " + nextLevelColor + nextSize,
                    "",
                    "§7Koszt ulepszenia: " + nextLevelColor + "64x " + Upgrade.translate(nextLevelAlias),
                    "",
                    canUpgradeString
            ).build();
            return;
        }
        this.upgradeItem = builder.setLore(
                "",
                "§7Obecny poziom: §e" + currentLevelColor + Upgrade.translate(currentLevelAlias),
                "§8› §7Rozmiar: §e" + currentLevelColor + currentLevelSize,
                "",
                "§7Osiągnąłeś najwyższy poziom terenu."
        ).build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(executor, "§f\u7000\u7002");
        // Setting menu items
        panel.setItem(12, StaticItems.FLAGS, event -> panel.applySection(new SectionFlags(panel, executor, owner, region)));
        panel.setItem(14, this.upgradeItem, event -> {
            if (nextLevelAlias != null) {
                if (executor.hasPermission("skydistrict.claims.bypass.upgradecost") || executor.getInventory().contains(upgradePrice)) {
                    executor.closeInventory();
                    if (!executor.hasPermission("skydistrict.claims.bypass.upgradecost")) {
                        executor.getInventory().removeItem(upgradePrice);
                    }
                    region.setType(ProtectionStones.getProtectBlockFromAlias(nextLevelAlias));
                    this.redefine(region.getWGRegion());
                    executor.sendMessage(Lang.UPGRADE_SUCCESS);
                    executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            }
        });
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, region)));
    }

    private void redefine(ProtectedRegion wgRegion) {
        String id = wgRegion.getId();
        // Min point
        BlockVector3 min = wgRegion.getMinimumPoint();
        BlockVector3 newMin = BlockVector3.at(min.getBlockX() - 5, min.getBlockY(), min.getBlockZ() - 5);
        // Max point
        BlockVector3 max = wgRegion.getMaximumPoint();
        BlockVector3 newMax = BlockVector3.at(max.getBlockX() + 5, max.getBlockY(), max.getBlockZ() + 5);
        // Creating cuboid at new points
        ProtectedRegion newRegion = new ProtectedCuboidRegion(id, newMin, newMax);
        // Redefining region
        newRegion.copyFrom(wgRegion);
        RegionAdder task = new RegionAdder(region.getWGRegionManager(), newRegion);
        try {
            task.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

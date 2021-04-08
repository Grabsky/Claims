package net.skydistrict.claimsgui.panel.sections;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import net.skydistrict.claimsgui.utils.Upgrade;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// TO-DO: Implement better items check; also, I feel like all of that can be written in less confusing way
public class SettingsSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private String nextLevelAlias;
    private ItemStack upgradePrice;
    private ItemStack upgradeItem;

    public SettingsSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        // I'm not sure why it's marked as nullable...
        String currentLevelAlias = (region.getTypeOptions() != null) ? region.getTypeOptions().alias : "";
        ChatColor currentLevelColor = Upgrade.color(currentLevelAlias);
        // Size of current level in readable format
        int currentLevelSizeCalc = Upgrade.getSize(currentLevelAlias) * 2 + 1;
        String currentLevelSize = currentLevelSizeCalc + "x" + currentLevelSizeCalc;
        // Item
        ItemBuilder builder = null;
        switch (currentLevelAlias) {
            case "COAL":
                builder = StaticItems.COAL_BLOCK;
                break;
            case "IRON":
                builder = StaticItems.IRON_BLOCK;
                break;
            case "GOLD":
                builder = StaticItems.GOLD_BLOCK;
                break;
            case "DIAMOND":
                builder = StaticItems.DIAMOND_BLOCK;
                break;
            case "EMERALD":
                builder = StaticItems.EMERALD_BLOCK;
                break;
            default:
                System.out.println(currentLevelAlias);
        }
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
            boolean canUpgrade = player.getInventory().contains(upgradePrice);
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
        } else {
            this.upgradeItem = builder.setLore(
                    "",
                    "§7Obecny poziom: §e" + currentLevelColor + Upgrade.translate(currentLevelAlias),
                    "§8› §7Rozmiar: §e" + currentLevelColor + currentLevelSize,
                    "",
                    "§7Osiągnąłeś najwyższy poziom terenu."
            ).build();
        }
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7002", Containers.GENERIC_9X6);
        // Setting menu items
        panel.setItem(12, StaticItems.FLAGS, event -> panel.applySection(new FlagsSection(panel, player, region)));
        panel.setItem(14, this.upgradeItem, event -> {
            if (nextLevelAlias != null) {
                if (player.getInventory().contains(upgradePrice)) {
                    player.closeInventory();
                    player.getInventory().removeItem(upgradePrice);
                    region.setType(ProtectionStones.getProtectBlockFromAlias(nextLevelAlias));
                    this.redefine(region.getWGRegion());
                }
            }
        });
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new MainSection(panel, player, region)));
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

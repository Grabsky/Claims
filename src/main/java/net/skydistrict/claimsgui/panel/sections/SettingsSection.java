package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import net.skydistrict.claimsgui.utils.Upgrade;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SettingsSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private String nextLevelType;
    private ItemStack upgradePrice;
    private ItemStack UPGRADE;

    public SettingsSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        String currentLevelType = region.getType();
        ChatColor currentLevelColor = Upgrade.color(currentLevelType);
        // Size of current level in readable format
        int currentSizeCalc = Upgrade.getSize(currentLevelType) * 2 + 1;
        String currentSize = currentSizeCalc + "x" + currentSizeCalc;
        // If current level is not the last
        if (!currentLevelType.equals("EMERALD")) {
            // What's the next level?
            this.nextLevelType = Upgrade.ORDER.get(Upgrade.ORDER.indexOf(currentLevelType) + 1);
            // Next level color
            ChatColor nextLevelColor = Upgrade.color(nextLevelType);
            // Size of next level in readable format
            int nextSizeCalc = Upgrade.getSize(nextLevelType) * 2 + 1;
            String nextSize = nextSizeCalc + "x" + nextSizeCalc;
            // Upgrade price
            this.upgradePrice = Upgrade.getUpgradePrice(currentLevelType);
            // Can you upgrade?
            boolean canUpgrade = player.getInventory().contains(upgradePrice);
            String canUpgradeString = canUpgrade ? "§7Kliknij, aby ulepszyć." : "§cNie stać cię na ulepszenie.";
            // ItemStack
            this.UPGRADE = StaticItems.UPGRADE.setLore(
                    "",
                    "§7Obecny poziom: " + currentLevelColor + Upgrade.translate(currentLevelType).toUpperCase(),
                    "§8› §7Rozmiar: " + currentLevelColor + currentSize,
                    "",
                    "§7Następny poziom: §e" + nextLevelColor + Upgrade.translate(nextLevelType).toUpperCase(),
                    "§8› §7Rozmiar: " + nextLevelColor + nextSize,
                    "",
                    "§7Koszt: " + nextLevelColor + "64x " + Upgrade.translate(nextLevelType),
                    "",
                    canUpgradeString
            ).build();
        } else {
            this.UPGRADE = StaticItems.UPGRADE.setLore(
                    "",
                    "§7Obecny poziom: §e" + currentLevelColor + Upgrade.translate(currentLevelType),
                    "§7Rozmiar: §e" + currentLevelColor + Upgrade.getSize(currentLevelType),
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
        panel.setItem(14, this.UPGRADE, event -> {
            if (nextLevelType != null) {
                if (player.getInventory().contains(upgradePrice)) {
                    player.closeInventory();
                    player.getInventory().remove(upgradePrice);
                    region.setType(ProtectionStones.getBlockOptions(this.nextLevelType));
                }
            }
        });
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new MainSection(panel, player, region)));
    }
}

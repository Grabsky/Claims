package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SettingsSection extends Section {
    private final Panel panel;
    private final Player player;
    private PSRegion region;
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
        this.UPGRADE = StaticItems.UPGRADE.setLore("§7Kliknij, aby ulepszyć.", "", "§7Następny poziom: §bDIAMENT", "§7Rozmiar: §b71x71", "", "§7Koszt: §b64x Diament").build();
    }

    @Override
    public void apply() {
        NMS.updateTitle(player, "§f\u7000\u7002", Containers.GENERIC_9X6);
        panel.setItem(12, StaticItems.FLAGS, event -> {
            // panel.applySection(new FlagsSection(panel, player, region));
        });
        panel.setItem(14, this.UPGRADE, event -> {
            // panel.applySection(new UpgradeSection(panel, player, region));
        });
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new MainSection(panel, player, region)));
    }
}

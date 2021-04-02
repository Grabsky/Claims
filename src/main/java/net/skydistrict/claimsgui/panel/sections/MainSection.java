package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.ItemBuilder;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public class MainSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;

    private ItemStack MEMBERS;
    private ItemStack INFO;

    public MainSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        this.MEMBERS = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullOwner(player.getUniqueId())
                .build();
        this.INFO = StaticItems.INFO.setLore("", "§8§l› §7Dom: §e" + region.getHome().getBlockX() + ", " + region.getHome().getBlockY() + ", " + region.getHome().getBlockZ(), "§8§l› §7Liczba członków: §e" + region.getMembers().size()).build();
    }

    @Override
    public void apply() {
        NMS.updateTitle(player, "§f\u7000\u7001", Containers.GENERIC_9X6);
        panel.setItem(10, StaticItems.HOMES, event -> {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                // LMB - teleport
            } else if (event.getAction() == InventoryAction.PICKUP_HALF);
                // RMB - list of teleports
        });
        panel.setItem(12, this.MEMBERS, event -> {
            panel.applySection(new MembersSection(panel, player, region));
        });
        panel.setItem(14, StaticItems.SETTINGS, event -> {
            panel.applySection(new SettingsSection(panel, player, region));
        });
        panel.setItem(16, this.INFO);
        panel.setItem(40, StaticItems.RETURN, (event) -> player.closeInventory());
    }
}

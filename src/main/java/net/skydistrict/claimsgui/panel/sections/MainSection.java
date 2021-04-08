package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public class MainSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;

    private ItemStack members;

    public MainSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        this.members = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullOwner(player.getUniqueId())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7001", Containers.GENERIC_9X6);
        // Setting menu items
        panel.setItem(11, StaticItems.HOMES, event -> {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                // LMB - teleport
            } else if (event.getAction() == InventoryAction.PICKUP_HALF);
                // RMB - list of teleports
        });
        panel.setItem(13, this.members, event -> panel.applySection(new MembersSection(panel, player, region)));
        panel.setItem(15, StaticItems.SETTINGS, event -> panel.applySection(new SettingsSection(panel, player, region)));
        panel.setItem(40, StaticItems.RETURN, (event) -> player.closeInventory());
    }
}

package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionMain extends Section {
    private ItemStack members;

    public SectionMain(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
    }

    @Override
    public void prepare() {
        this.members = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullOwner(executor.getUniqueId())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7101", editMode);
        // Setting menu items
        panel.setItem(11, StaticItems.HOMES, (event) -> panel.applySection(new SectionHomes(panel, executor, owner, region)));
        panel.setItem(13, this.members, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, region)));
        panel.setItem(15, StaticItems.SETTINGS, (event) -> panel.applySection(new SectionSettings(panel, executor, owner, region)));
        panel.setItem(49, StaticItems.RETURN, (event) -> executor.closeInventory());
    }
}

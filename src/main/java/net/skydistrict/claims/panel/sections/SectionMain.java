package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionMain extends Section {
    private ItemStack members;

    public SectionMain(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        this.members = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullValue(UserCache.get(owner).getSkullValue())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7101", editMode);
        // Setting menu items
        panel.setItem(11, Items.HOMES, (event) -> panel.applySection(new SectionHomes(panel, executor, owner, claim)));
        panel.setItem(13, members, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, claim)));
        panel.setItem(15, Items.SETTINGS, (event) -> panel.applySection(new SectionSettings(panel, executor, owner, claim)));
        panel.setItem(49, Items.RETURN, (event) -> executor.closeInventory());
    }
}

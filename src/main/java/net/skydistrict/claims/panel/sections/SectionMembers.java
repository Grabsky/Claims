package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SectionMembers extends Section {

    public SectionMembers(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        // Nothing to prepare this time :)
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7104", editMode);
        // Generating the view
        this.generateView();
    }

    private void generateView() {
        panel.clear();
        // For each added member slot
        int slot = 11;
        for (UUID uuid : claim.getMembers()) {
            // Add skull to gui
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§c§l" + UUIDCache.get(uuid))
                    .setLore("§7Kliknij, aby §cwyrzucić§7 z terenu.")
                    .setSkullOwner(uuid)
                    .build(), event -> {
                // One more check just in case something changed while GUI was open
                if (claim.removeMember(uuid)) {
                    this.generateView();
                } else {
                    executor.closeInventory();
                    Lang.send(executor, Lang.NOT_MEMBER);
                }
            });
            slot = (slot == 15) ? 20 : slot + 1;
        }
        if (slot != 25) panel.setItem(slot, Items.ADD, event -> panel.applySection(new SectionMembersAdd(panel, executor, owner, claim)));
        panel.setItem(49, Items.RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, claim)));
    }
}

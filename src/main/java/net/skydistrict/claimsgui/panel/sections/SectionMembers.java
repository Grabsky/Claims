package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class SectionMembers extends Section {

    public SectionMembers(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
    }

    @Override
    public void prepare() {
        // Nothing to prepare
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(executor, "§f\u7000\u7004");
        // Generating the view
        this.generateView();
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMain(panel, executor, owner, region)));
    }

    private void generateView() {
        panel.clear();
        // Getting list of members
        final ArrayList<UUID> members = region.getMembers();
        // For each added member slot
        int slot = 11;
        for (UUID uuid : members) {
            // Add skull to gui
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§c§l" + UUIDCache.getNameFromUUID(uuid))
                    .setLore("§7Kliknij, aby §cwyrzucić§7 z terenu.")
                    .setSkullOwner(uuid)
                    .build(), event -> {
                // One more check just in case something changed while GUI was open
                if (region.getMembers().contains(uuid)) {
                    region.removeMember(uuid);
                    this.generateView();
                } else {
                    executor.closeInventory();
                    executor.sendMessage("§6§lS§e§lD§8 » §cTen gracz nie jest dodany do terenu.");
                }

            });
            slot = (slot == 15) ? 20 : slot + 1;
        }
        if (slot != 25) {
            panel.setItem(slot, StaticItems.ADD, event -> panel.applySection(new SectionMembersAdd(panel, executor, owner, region)));
        }

    }
}

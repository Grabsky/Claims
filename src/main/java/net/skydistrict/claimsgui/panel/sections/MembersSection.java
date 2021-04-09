package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class MembersSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;

    public MembersSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
    }

    @Override
    public void prepare() {
        // Nothing to prepare for this section
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7004", Containers.GENERIC_9X6);
        // Generating the view
        this.generateView();
    }

    // TO-DO: Players are shown two times - FIX THIS
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
                    player.closeInventory();
                    player.sendMessage("§6§lS§e§lD§8 » §cTen gracz nie jest dodany do terenu.");
                }

            });
            slot = (slot == 15) ? 20 : slot + 1;
        }
        if (slot != 25) {
            panel.setItem(slot, StaticItems.ADD, event -> panel.applySection(new MembersAddSection(panel, player, region)));
        }
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new MainSection(panel, player, region)));
    }
}

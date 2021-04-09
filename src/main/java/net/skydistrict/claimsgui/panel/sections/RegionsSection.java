package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.Lang;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import net.skydistrict.claimsgui.utils.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

// Not sure about the limits of 5 regions displayed. Maybe make it page-able or something.
public class RegionsSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;

    public RegionsSection(Panel panel, Player player, PSRegion region) {
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
        NMS.updateTitle(player, "§f\u7000\u7006", Containers.GENERIC_9X6);
        this.generateView();
    }

    private void generateView() {
        List<PSRegion> regions = PSPlayer.fromPlayer(player).getPSRegions(Bukkit.getWorlds().get(0), true);
        int slot = 11;
        for (PSRegion reg : regions) {
            if (slot < 16) {
                if (reg.getOwners().contains(player.getUniqueId())) continue;
                UUID ownerUuid = reg.getOwners().get(0);
                panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                        .setName("§e§l" + UUIDCache.getNameFromUUID(ownerUuid))
                        .setLore("§7Kliknij, aby się teleportować.")
                        .setSkullOwner(ownerUuid)
                        .build(), event -> {
                    player.closeInventory();
                    player.sendMessage(Lang.TELEPORTING);
                    Teleport.teleport(player, reg.getHome(), 5);
                });
            }
            slot++;
        }

    }
}

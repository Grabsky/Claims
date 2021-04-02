package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MembersSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private String unableAddReason = null;
    private String unableRemoveReason = null;

    public MembersSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        // ADD
        if (region.getMembers().size() >= 8) {
            this.unableAddReason = "§7Osiągnąłeś limit (8) graczy dodanych do terenu.";
        } else if (Bukkit.getOnlinePlayers().size() == 1) {
            this.unableAddReason = "§7Aktualnie nie ma nikogo na serwerze.";
        }
        // REMOVE
        if (region.getMembers().size() == 0) {
            this.unableRemoveReason = "§7Nie masz kogo usunąć.";
        }
    }

    @Override
    public void apply() {
        NMS.updateTitle(player, "§f\u7000\u7002", Containers.GENERIC_9X6);
        // ADD
        if (unableAddReason == null) {
            panel.setItem(12, StaticItems.ADD, event -> {
                panel.applySection(new MembersAddSection(panel, player, region));
            });
        } else {
            panel.setItem(12, StaticItems.ADD_DISABLED.setLore(unableAddReason).build());
        }
        // REMOVE
        if (unableRemoveReason == null) {
            panel.setItem(14, StaticItems.REMOVE, event -> {
                // panel.applySection(new MembersRemoveSection(panel, player, region));
            });
        } else {
            panel.setItem(14, StaticItems.REMOVE_DISABLED.setLore(unableRemoveReason).build());
        }
        // RETURN
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new MainSection(panel, player, region)));
    }
}

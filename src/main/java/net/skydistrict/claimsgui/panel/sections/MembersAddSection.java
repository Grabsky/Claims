package net.skydistrict.claimsgui.panel.sections;

import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticMessages;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MembersAddSection extends Section {
    private final Panel panel;
    private final Player player;

    public MembersAddSection(Panel panel, Player player) {
        super(panel, player);
        this.panel = panel;
        this.player = player;
    }


    @Override
    public void prepare() {

    }

    @Override
    public void apply() {
        NMS.updateTitle(player, "§f\u7000\u7002", Containers.GENERIC_9X6);
        panel.setItem(10, new ItemStack(Material.DIAMOND), event -> {
            player.sendMessage(StaticMessages.MESSAGE);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        });
    }
}

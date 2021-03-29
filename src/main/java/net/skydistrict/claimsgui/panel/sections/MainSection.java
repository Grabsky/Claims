package net.skydistrict.claimsgui.panel.sections;

import net.skydistrict.claimsgui.config.StaticMessages;
import net.skydistrict.claimsgui.panel.Panel;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainSection extends Section {

    public MainSection(Panel panel, Player player) {
        super(panel, player);
    }

    @Override
    public void load() {
        Panel panel = this.getPanel();
        panel.setItem(0, new ItemStack(Material.DIAMOND), event -> {
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(StaticMessages.MESSAGE);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        });
    }
}

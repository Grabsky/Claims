package net.skydistrict.claimsgui.panel.sections;

import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainSection extends Section {
    private final Panel panel;
    private final Player player;

    public MainSection(Panel panel, Player player) {
        super(panel, player);
        this.panel = panel;
        this.player = player;
    }


    @Override
    public void prepare() {

    }

    @Override
    public void apply() {
        panel.setItem(10, StaticItems.MAIN_HOME, event -> {
            panel.applySection(new MembersAddSection(panel, player));
        });
    }
}

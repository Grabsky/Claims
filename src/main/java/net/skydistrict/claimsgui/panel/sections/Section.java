package net.skydistrict.claimsgui.panel.sections;

import net.skydistrict.claimsgui.panel.Panel;
import org.bukkit.entity.Player;

public abstract class Section {
    private final Panel panel;
    private final Player player;

    public Section(Panel panel, Player player) {
        this.panel = panel;
        this.player = player;
        this.panel.clear();
        this.load();
    }

    public Panel getPanel() {
        return panel;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract void load();
}

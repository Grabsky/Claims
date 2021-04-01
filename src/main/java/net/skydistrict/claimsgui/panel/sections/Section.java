package net.skydistrict.claimsgui.panel.sections;

import net.skydistrict.claimsgui.panel.Panel;
import org.bukkit.entity.Player;

public abstract class Section {

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player player) {
        panel.clear();
    }

    /** Here, you can prepare your section before applying it */
    public abstract void prepare();

    /** Here, you can make changes in the Panel*/
    public abstract void apply();
}

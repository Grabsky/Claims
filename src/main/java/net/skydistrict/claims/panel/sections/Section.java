package net.skydistrict.claims.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.skydistrict.claims.panel.Panel;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Section {
    protected Panel panel;
    protected Player executor;
    protected UUID owner;
    protected PSRegion region;
    protected boolean editMode = false;

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player executor, UUID owner) {
        this.panel = panel;
        this.executor = executor;
        this.owner = owner;
        if (executor.getUniqueId() != owner) editMode = true;
        this.panel.clear();
    }

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player executor, UUID owner, PSRegion region) {
        this.panel = panel;
        this.executor = executor;
        this.owner = owner;
        this.region = region;
        if (executor.getUniqueId() != owner) editMode = true;
        this.panel.clear();
    }

    /** Here, you can prepare your section before applying it */
    public abstract void prepare();

    /** Here, you can make changes to the Panel */
    public abstract void apply();
}

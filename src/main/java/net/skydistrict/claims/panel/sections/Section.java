package net.skydistrict.claims.panel.sections;

import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.panel.Panel;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Section {
    protected Panel panel;
    protected Player executor;
    protected UUID owner;
    protected Claim claim;
    protected boolean editMode = false;

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player executor, UUID owner) {
        this.panel = panel;
        this.executor = executor;
        this.owner = owner;
        if (!executor.getUniqueId().equals(owner)) {
            editMode = true;
        }
        this.panel.clear();
    }

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player executor, UUID owner, Claim claim) {
        this.panel = panel;
        this.executor = executor;
        this.owner = owner;
        this.claim = claim;
        if (!executor.getUniqueId().equals(owner)) {
            editMode = true;
        }
        this.panel.clear();
    }

    /** Here, you can prepare your section before applying it */
    public abstract void prepare();

    /** Here, you can make changes to the Panel */
    public abstract void apply();
}

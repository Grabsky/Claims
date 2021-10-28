package me.grabsky.claims.panel.sections;

import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.panel.Panel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Section {
    protected final Panel panel;
    protected final Player viewer;
    protected final UUID claimOwnerUniqueId;
    protected final boolean editMode;
    protected Claim claim;

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(final Panel panel, final Player viewer, final UUID claimOwnerUniqueId, @Nullable Claim claim) {
        this.panel = panel;
        this.viewer = viewer;
        this.claimOwnerUniqueId = claimOwnerUniqueId;
        this.claim = claim;
        this.editMode = !viewer.getUniqueId().equals(claimOwnerUniqueId);
        this.panel.clear();
    }

    /** Here, you can prepare your section before applying it */
    public abstract void prepare();

    /** Here, you can make changes to the Panel */
    public abstract void apply();
}
package me.grabsky.claims.panel.sections;

import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.panel.Panel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Section {
    protected final Panel panel;
    protected final Player executor;
    protected final UUID owner;
    protected Claim claim;
    protected boolean editMode = false;

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(Panel panel, Player executor, UUID owner, @Nullable Claim claim) {
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

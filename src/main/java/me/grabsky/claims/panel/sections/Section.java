package me.grabsky.claims.panel.sections;

import me.grabsky.claims.panel.Panel;

public abstract class Section {
    protected final Panel panel;

    /** Constructor required to prepare and apply Section to a Panel */
    public Section(final Panel panel) {
        this.panel = panel;
        this.panel.clear();
    }

    /** Here, you can prepare your section before applying it */
    public abstract void prepare();

    /** Here, you can make changes to the Panel */
    public abstract void apply();
}
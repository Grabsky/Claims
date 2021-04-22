package net.skydistrict.claims.panel;

import net.kyori.adventure.text.Component;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.interfaces.ClickAction;
import net.skydistrict.claims.panel.sections.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Panel {
    private static final PanelManager panelManager = Claims.getInstance().getPanelManager();
    private final Inventory inventory;
    private final int size;
    private ClickAction[] actions;

    /** Construtor required to create Panel object */
    public Panel(int size, @NotNull Component title) {
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.actions = new ClickAction[size];
    }

    /** Construtor required to create Panel object */
    public Panel(int size, @NotNull String title) {
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, Component.text(title));
        this.actions = new ClickAction[size];
    }

    /** Returns this inventory */
    public Inventory getInventory() {
        return this.inventory;
    }

    /** Sets item in panel inventory */
    public void setItem(int slot, @NotNull ItemStack item, @Nullable ClickAction action) {
        this.inventory.setItem(slot, item);
        if (action != null) {
            actions[slot] = action;
        }
    }

    /** Sets item in panel inventory */
    public void setItem(int slot, @NotNull ItemStack item) {
        this.inventory.setItem(slot, item);
    }

    /** Returns ClickAction (callback) for specified slot */
    @Nullable public ClickAction getAction(int slot) {
        return actions[slot];
    }

    /** Clears contents of this panel (including callbacks) */
    public void clear() {
        this.inventory.clear();
        this.actions = new ClickAction[size];
    }

    /** Opens panel to player */
    public void open(Player player) {
        panelManager.add(player, this);
        player.openInventory(this.inventory);
    }

    /** Opens panel to player */
    public void applySection(Section section) {
        section.prepare();
        section.apply();
    }
}
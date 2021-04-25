package net.skydistrict.claims.panel;

import net.kyori.adventure.text.Component;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.interfaces.ClickTrigger;
import net.skydistrict.claims.panel.sections.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Panel {
    private static final PanelManager panelManager = Claims.getInstance().getPanelManager();
    private final Inventory inventory;
    private final int size;
    private ClickTrigger[] triggers;

    /** Construtor required to create Panel object */
    public Panel(int size, @NotNull Component title) {
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.triggers = new ClickTrigger[size];
    }

    /** Construtor required to create Panel object */
    public Panel(int size, @NotNull String title) {
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, Component.text(title));
        this.triggers = new ClickTrigger[size];
    }

    /** Returns this inventory */
    public Inventory getInventory() {
        return this.inventory;
    }

    /** Sets item in panel inventory */
    public void setItem(int slot, @NotNull ItemStack item, @NotNull ClickTrigger action) {
        this.inventory.setItem(slot, item);
        triggers[slot] = action;
    }

    /** Sets item in panel inventory */
    public void setItem(int slot, @NotNull ItemStack item) {
        this.inventory.setItem(slot, item);
    }

    /** Returns ClickAction (callback) for specified slot */
    @Nullable public ClickTrigger getTrigger(int slot) {
        return triggers[slot];
    }

    /** Clears contents of this panel (including callbacks) */
    public void clear() {
        this.inventory.clear();
        this.triggers = new ClickTrigger[size];
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
        for (HumanEntity e : inventory.getViewers()) {
            ((Player) e).updateInventory();
        }
    }
}
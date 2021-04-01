package net.skydistrict.claimsgui.panel;

import net.kyori.adventure.text.Component;
import net.skydistrict.claimsgui.ClaimsGUI;
import net.skydistrict.claimsgui.panel.interfaces.ClickAction;
import net.skydistrict.claimsgui.panel.sections.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Panel {
    private static final PanelManager panelManager = ClaimsGUI.getInstance().getPanelManager();
    private final Inventory inventory;
    private final int size;
    private ClickAction[] actions;

    /** Construtor required to create Panel object */
    public Panel(int size, @NotNull Component title) {
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
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
        section.apply();
    }

}

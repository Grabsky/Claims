package net.skydistrict.claimsgui.panel;

import net.kyori.adventure.text.Component;
import net.skydistrict.claimsgui.ClaimsGUI;
import net.skydistrict.claimsgui.panel.sections.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Panel {
    private static final PanelManager panelManager = ClaimsGUI.getInstance().getPanelManager();
    private final Inventory inventory;
    private final Map<Integer, ClickAction> actions;

    public Panel(int size, @NotNull Component title) {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.actions = new HashMap<Integer, ClickAction>();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setItem(int slot, @NotNull ItemStack item, @Nullable ClickAction action) {
        this.inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    @Nullable
    public ClickAction getAction(int slot) {
        return actions.get(slot);
    }

    public void clear() {
        this.inventory.clear();
        this.actions.clear();
    }

    public void applySection(Section section) {
        this.clear();
        section.load();
    }

    public void open(Player player) {
        panelManager.add(player, this);
        player.openInventory(this.inventory);
    }
}

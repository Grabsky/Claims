package me.grabsky.claims.panel;

import me.grabsky.claims.Claims;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class PanelManager implements Listener {
    private final Map<Player, Panel> openInventories;
    private final Map<Player, Long> clickCooldowns;

    public PanelManager(Claims instance) {
        this.openInventories = new HashMap<>();
        this.clickCooldowns = new HashMap<>();
        // Registering events
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public boolean isInventoryOpen(Player player) {
        return openInventories.containsKey(player);
    }

    public void add(Player player, Panel panel) {
        this.openInventories.put(player, panel);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (openInventories.containsKey(player)) {
            event.setCancelled(true);
            // Return if clicked slot is outside the inventory or empty
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            final Panel panel = openInventories.get(player);
            // Return if click type is not supported or player is not inside a section
            final Inventory inventory = panel.getInventory();
            if (event.getClickedInventory() == inventory) {
                // Return if player is on cooldown
                if (clickCooldowns.containsKey(player) && (System.currentTimeMillis() - clickCooldowns.get(player)) < 350) return;
                // Updating cooldown
                clickCooldowns.put(player, System.currentTimeMillis());
                final int slot = event.getSlot();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1F, 1.5F);
                if (panel.getTrigger(slot) != null) {
                    panel.getTrigger(slot).click(event);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openInventories.remove((Player) event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        openInventories.remove(event.getPlayer());
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        if (openInventories.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

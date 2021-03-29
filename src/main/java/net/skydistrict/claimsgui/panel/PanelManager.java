package net.skydistrict.claimsgui.panel;

import net.skydistrict.claimsgui.ClaimsGUI;
import net.skydistrict.claimsgui.config.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class PanelManager implements Listener {
    private Map<Player, Panel> openInventories;
    private Map<Player, Long> clickCooldowns;

    public PanelManager(ClaimsGUI instance) {
        this.openInventories = new HashMap<Player, Panel>();
        this.clickCooldowns = new HashMap<Player, Long>();
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void add(Player player, Panel panel) {
        this.openInventories.put(player, panel);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        System.out.println(this.openInventories);
        if (openInventories.containsKey(player)) {
            event.setCancelled(true);
            // Return if clicked outside inventory or empty slot
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Panel panel = openInventories.get(player);
            Inventory inventory = panel.getInventory();
            if (event.getClickedInventory() == inventory) {
                // Return if player is on cooldown
                if (clickCooldowns.containsKey(player) && (System.currentTimeMillis() - clickCooldowns.get(player)) < Config.GUI_COOLDOWN_MS) return;
                clickCooldowns.put(player, System.currentTimeMillis());
                int slot = event.getSlot();
                if (panel.getAction(slot) != null) {
                    panel.getAction(slot).click(event);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.openInventories.remove((Player) event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.openInventories.remove(event.getPlayer());
    }
}

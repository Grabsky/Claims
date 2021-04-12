package net.skydistrict.claims.utils;

import net.minecraft.server.v1_16_R3.Containers;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindow;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryH {

    /**
     * Updates title of inventory currently open by specified player
     */
    public static void updateTitle(Player player, String title, boolean editMode) {
        String finTitle = (editMode) ? title + "\u7001§r*" : title;
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                handle.activeContainer.windowId,
                Containers.GENERIC_9X6,
                IChatBaseComponent.ChatSerializer.jsonToComponent("{\"text\": \"" + finTitle + "\"}")
        );
        handle.playerConnection.sendPacket(packet);
        handle.updateInventory(handle.activeContainer);
    }


    /**
     * Removes specific amount of items (compared by type) from player's inventory
     */
    public static void removeMaterial(final Player player, final Material material, final int amount) {
        int leftToRemove = amount;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (leftToRemove == 0) break;
            if (item != null && item.getType() == material) {
                int a = Math.min(item.getAmount(), leftToRemove);
                leftToRemove -= a;
                item.setAmount(item.getAmount() - a);
            }
        }
    }

    /**
     * Returns true if player has given amount of specific material in his inventory
     */
    public static boolean hasMaterial(final Player player, final Material material, final int amount) {
        int found = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (found >= amount) return true;
            if (item != null && item.getType() == material) found += item.getAmount();
        }
        return false;
    }
}
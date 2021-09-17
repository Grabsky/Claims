package me.grabsky.claims.utils;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.inventory.Containers;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {
    // Updates title of inventory currently open by specified player
    public static void updateTitle(Player player, String title, boolean editMode) {
        final String finalTitle = (editMode) ? title + "\u7001§r*" : title;
        final EntityPlayer handle = ((CraftPlayer) player).getHandle();
        final PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                handle.bV.j,
                Containers.f, // GENERIC_9X6
                IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + finalTitle + "\"}")
        );
        handle.b.sendPacket(packet);
        player.updateInventory();
    }

    // Removes specific amount of items (compared by type) from player's inventory
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

    // Returns true if player has given amount of specific material in his inventory
    public static boolean hasMaterial(final Player player, final Material material, final int amount) {
        int found = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (found >= amount) return true;
            if (item != null && item.getType() == material) {
                found += item.getAmount();
            }
        }
        return false;
    }

}

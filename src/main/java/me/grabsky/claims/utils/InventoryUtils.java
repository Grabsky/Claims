package me.grabsky.claims.utils;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.inventory.Containers;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InventoryUtils {
    // Updates title of inventory currently open for player
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
}

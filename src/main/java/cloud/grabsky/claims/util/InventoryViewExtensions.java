package cloud.grabsky.claims.util;

import io.papermc.paper.adventure.AdventureComponent;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

// To be used with Lombok's @ExtensionMethod annotation.
public final class InventoryViewExtensions {

    public static void title(final @NotNull InventoryView view, final @NotNull Component component) {
        // Getting the ServerPlayer instance from InventoryView#getPlayer.
        final ServerPlayer player = ((CraftPlayer) view.getPlayer()).getHandle();
        // Creating title change packet.
        final ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                player.containerMenu.containerId, // ID of the currently open container.
                player.containerMenu.getType(), // MenuType of currently open container.
                new AdventureComponent(component)
        );
        // Sending the packet.
        player.connection.send(packet);
    }

}

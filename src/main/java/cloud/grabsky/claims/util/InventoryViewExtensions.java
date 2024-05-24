/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims.util;

import io.papermc.paper.adventure.AdventureComponent;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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
        // Sending inventory update to the player.
        player.containerMenu.sendAllDataToRemote();
    }

}

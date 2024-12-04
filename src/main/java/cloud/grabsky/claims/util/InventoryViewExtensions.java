/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
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

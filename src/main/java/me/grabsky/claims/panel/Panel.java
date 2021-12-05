package me.grabsky.claims.panel;

import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.panel.sections.Section;
import me.grabsky.indigo.framework.inventories.ExclusiveInventory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class Panel extends ExclusiveInventory {
    private boolean editMode = false;
    private ClaimPlayer claimPlayer;

    public static Sound CLICK_SOUND = Sound.sound(Key.key("block.note_block.hat"), Sound.Source.MASTER, 1f, 1.5f);

    public Panel(final net.kyori.adventure.text.Component title, final int size) {
        super(title, size);
    }

    public Panel(final net.kyori.adventure.text.Component title, final int size, final Sound clickSound) {
        super(title, size, clickSound);
    }

    public Panel(final net.kyori.adventure.text.Component title, final ClaimPlayer claimPlayer, final boolean editMode) {
        super(title, 54, CLICK_SOUND);
        this.claimPlayer = claimPlayer;
        this.editMode = editMode;
    }

    public void applySection(final Section section) {
        section.prepare();
        section.apply();
        for (final HumanEntity human : inventory.getViewers()) {
            ((Player) human).updateInventory();
        }
    }

    public void updateClientTitle(final String title) {
        final Player player = (Player) inventory.getViewers().get(0);
        final String finalTitle = (editMode) ? title + "\u7001§r*" : title;
        final ServerPlayer handle = ((CraftPlayer) player).getHandle();
        final net.minecraft.network.protocol.game.ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                handle.containerMenu.containerId, // Active container id
                MenuType.GENERIC_9x6, // GENERIC_9X6 (54 slots)
                net.minecraft.network.chat.Component.Serializer.fromJson("{\"text\": \"" + finalTitle + "\"}") // Safe to ignore
        );
        handle.connection.send(packet);
        player.updateInventory();
    }

    // Only one player SHOULD be viewing inventory at time this method is called
    public Player getViewer() {
        return (Player) inventory.getViewers().get(0);
    }

    public ClaimPlayer getClaimOwner() {
        return claimPlayer;
    }
}
package me.grabsky.claims.panel;

import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.panel.sections.Section;
import me.grabsky.indigo.framework.inventories.ExclusiveInventory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.inventory.Containers;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class Panel extends ExclusiveInventory {
    private boolean editMode = false;
    private ClaimPlayer claimPlayer;

    public static Sound CLICK_SOUND = Sound.sound(Key.key("block.note_block.hat"), Sound.Source.MASTER, 1f, 1.5f);

    public Panel(final Component title, final int size) {
        super(title, size);
    }

    public Panel(final Component title, final int size, final Sound clickSound) {
        super(title, size, clickSound);
    }

    public Panel(final Component title, final ClaimPlayer claimPlayer, final boolean editMode) {
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
        final EntityPlayer handle = ((CraftPlayer) player).getHandle();
        final PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                handle.bV.j, // No idea what's this
                Containers.f, // GENERIC_9X6 (54 slots)
                IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + finalTitle + "\"}") // Should be safe to ignore
        );
        handle.b.sendPacket(packet);
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
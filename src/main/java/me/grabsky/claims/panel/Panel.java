package me.grabsky.claims.panel;

import me.grabsky.claims.panel.sections.Section;
import me.grabsky.indigo.framework.inventories.ExclusiveInventory;
import me.grabsky.indigo.utils.Benchmarks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class Panel extends ExclusiveInventory {
    public static Sound CLICK_SOUND = Sound.sound(Key.key("block.note_block.hat"), Sound.Source.MASTER, 1f, 1.5f);

    public Panel(final Component title, final int size) {
        super(title, size);
    }

    public Panel(final Component title, final int size, final Sound clickSound) {
        super(title, size, clickSound);
    }

    public void applySection(final Section section) {
        final double s = Benchmarks.simple(() -> {
            section.prepare();
            section.apply();
            for (final HumanEntity human : inventory.getViewers()) {
                ((Player) human).updateInventory();
            }
        }, 1);
        System.out.println(s);
    }
}
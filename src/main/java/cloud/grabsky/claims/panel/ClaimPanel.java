package cloud.grabsky.claims.panel;

import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.panel.views.ViewMain;
import io.papermc.paper.adventure.AdventureComponent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.sound.Sound.sound;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class ClaimPanel extends Panel {

    @Getter(AccessLevel.PUBLIC)
    private final Claim claim;

    @Getter(AccessLevel.PUBLIC)
    private final ClaimPlayer editor;
    
    public Player getViewer() {
        // ...not sure if needed but just in case
        if (this.getInventory().getViewers().size() > 1)
            throw new IllegalStateException("Only one player can view claim panel at one time.");
        // ...
        return (Player) this.getInventory().getViewers().get(0);
    }

    public static Component INVENTORY_TITLE = text("\u7000\u7100", NamedTextColor.WHITE);

    public static Sound CLICK_SOUND = sound(key("block.note_block.hat"), Sound.Source.MASTER, 1f, 1.5f);

    // constr
    public ClaimPanel(final Claim claim, final ClaimPlayer editor) {
        super(INVENTORY_TITLE, 54, (event) -> event.getWhoClicked().playSound(CLICK_SOUND));
        // ...
        this.claim = claim;
        this.editor = editor;
    }
    
    public void applyView(final ClaimPanel.View view, boolean clearCurrent) {
        if (clearCurrent == true) {
            this.getInventory().clear();
        }
        view.accept(this);
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public abstract static class View implements Consumer<ClaimPanel> { /* ... */ }

    public void updateClientTitle(final Component title) {
        // adding '*' to the title if modifying unowned claim
        final Component finalTitle = (editor.isOwnerOf(claim) == false) ? empty().append(title).append(text("\u7001*")) : title;
        final ServerPlayer handle = ((CraftPlayer) this.getViewer()).getHandle();
        final ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                handle.containerMenu.containerId, // Active container id
                MenuType.GENERIC_9x6, // GENERIC_9X6 (54 slots)
                new AdventureComponent(finalTitle)
        );
        handle.connection.send(packet);
        this.getViewer().updateInventory();
    }

}
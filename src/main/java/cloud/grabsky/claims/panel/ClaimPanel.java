package cloud.grabsky.claims.panel;

import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.exception.ClaimProcessException;
import io.papermc.paper.adventure.AdventureComponent;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.sound.Sound.sound;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public final class ClaimPanel extends Panel {

    @Getter(AccessLevel.PUBLIC)
    public final ClaimManager manager;

    @Getter(AccessLevel.PUBLIC)
    private final Claim claim;

    public static Component INVENTORY_TITLE = text("\u7000\u7100", NamedTextColor.WHITE);
    public static ClickAction ACTION_PLAY_SOUND = new ClickAction() {

        private static final Sound SOUND = sound(key("block.note_block.hat"), Sound.Source.MASTER, 1f, 1.5f);

        @Override
        public void accept(final InventoryClickEvent event) {
            event.getWhoClicked().playSound(SOUND);
        }

    };

    public ClaimPanel(final ClaimManager manager, final Claim claim) {
        super(INVENTORY_TITLE, 54, ACTION_PLAY_SOUND);
        // ...
        this.manager = manager;
        this.claim = claim;
    }

    public Player getViewer() {
        // ...not sure if needed but just in case
        if (this.getInventory().getViewers().size() > 1)
            throw new IllegalStateException("Only one player can view claim panel at one time.");
        // ...
        return (Player) this.getInventory().getViewers().get(0);
    }

    public void updateClientTitle(final Component title) {
        final ClaimPlayer editor = manager.getClaimPlayer(this.getViewer());
        // adding '*' to the title if modifying unowned claim
        final Component finalTitle = (editor.isOwnerOf(claim) == false) ? empty().append(title).append(text("\u7001*")) : title;
        final ServerPlayer handle = ((CraftPlayer) this.getViewer()).getHandle();
        final ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                handle.containerMenu.containerId, // Active container id
                MenuType.GENERIC_9x6, // GENERIC_9x6 (54 slots)
                new AdventureComponent(finalTitle)
        );
        handle.connection.send(packet);
        this.getViewer().updateInventory();
    }

    public static boolean isClaimPanel(final InventoryView view) {
        return view.getTopInventory().getHolder() instanceof ClaimPanel;
    }

    public static void registerListener(final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            private final HashMap<UUID, Long> cooldowns = new HashMap<>();

            @EventHandler
            public void onPanelInventoryClick(final InventoryClickEvent event) {
                // Ignoring clicks outside inventory
                if (event.getClickedInventory() == null) return;
                // Ignoring non-panel inventories
                if (event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel panel) {
                    try {
                        // Since we're dealing with a custom inventory, cancelling the event is necessary
                        event.setCancelled(true);
                        // Ignoring clicks outside of the Panel inventory
                        if (event.getClickedInventory().getHolder() != panel) return;
                        // Returning if no acction is associated with clicked slot
                        if (panel.getActions().get(event.getSlot()) == null) return;
                        // Handling cooldown
                        final long lastClick = cooldowns.getOrDefault(event.getWhoClicked().getUniqueId(), 0L);
                        if (lastClick != 0L && (System.currentTimeMillis() - lastClick) < 150L) return;
                        // Updating cooldown
                        cooldowns.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
                        // PLaying sound if allowed
                        if (panel.getClickAction() != null) {
                            panel.getClickAction().accept(event);
                        }
                        // Executing action associated with clicked slot (aka clicked item)
                        panel.getActions().get(event.getSlot()).accept(event);
                    } catch (final ClaimProcessException e) {
                        sendMessage(event.getWhoClicked(), e.getErrorMessage());
                        panel.close();
                    }
                }
            }

        }, plugin);
    }

}
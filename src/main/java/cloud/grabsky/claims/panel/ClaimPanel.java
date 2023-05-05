package cloud.grabsky.claims.panel;

import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import io.papermc.paper.adventure.AdventureComponent;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public final class ClaimPanel extends Panel {

    @Getter(AccessLevel.PUBLIC)
    public final ClaimManager manager;

    @Getter(AccessLevel.PUBLIC)
    private final Claim claim;

    public static final Component INVENTORY_TITLE = text("\u7000\u7100", NamedTextColor.WHITE);

    public static final ClickAction ACTION_PLAY_SOUND = (event) -> {
        if (PluginConfig.UI_CLICK_SOUND != null)
            event.getWhoClicked().playSound(PluginConfig.UI_CLICK_SOUND);
    };

    public static final class Builder extends Panel.Builder<ClaimPanel> {

        private Claim claim;

        public @NotNull Builder setClaim(final @NotNull Claim claim) {
            this.claim = claim; return this.self();
        }

        private ClaimManager claimManager;

        public @NotNull Builder setClaimManager(final @NotNull ClaimManager claimManager) {
            this.claimManager = claimManager; return this.self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ClaimPanel build() {
            return new ClaimPanel(INVENTORY_TITLE, InventoryType.CHEST, 5,
                    // OPEN ACTION
                    (event) -> {
                        if (event.getInventory().getViewers().size() != 1)
                            event.setCancelled(true);
                    },
                    // NO CLOSE ACTION
                    (event) -> {},
                    // CLICK ACTION
                    (event) -> {
                        if (PluginConfig.UI_CLICK_SOUND != null)
                            event.getWhoClicked().playSound(PluginConfig.UI_CLICK_SOUND);
                    },
                    claimManager,
                    claim
            );
        }
    }

    public Player getViewer() {
        return (Player) this.getInventory().getViewers().get(0);
    }

    public void applyClaimTemplate(@NotNull final Consumer<ClaimPanel> template, final boolean clearCurrent) {
        // Clearing inventory before applying template (if requested)
        if (clearCurrent == true)
            this.clear();
        // Applying the template.
        template.accept(this);
    }

    private ClaimPanel(@NotNull final Component title,
                       final @NotNull InventoryType type,
                       final @Range(from = 0L, to = 5L) int rows,
                       final @NotNull Consumer<InventoryOpenEvent> onInventoryOpen,
                       final @NotNull Consumer<InventoryCloseEvent> onInventoryClose,
                       final @NotNull Consumer<InventoryClickEvent> onInventoryClick,
                       final @NotNull ClaimManager claimManager,
                       final @Nullable Claim claim
    ) {
        super(title, type, rows, onInventoryOpen, onInventoryClose, onInventoryClick);
        // ...
        this.manager = claimManager;
        this.claim = claim;
    }

    public void updateClientTitle(final Component title) {
        final ClaimPlayer editor = manager.getClaimPlayer(this.getViewer());
        // adding '*' to the title if modifying unowned claim
        final Component finalTitle = (claim != null && editor.isOwnerOf(claim) == false) ? empty().append(title).append(text("\u7001*")) : title;
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

    public static boolean isClaimPanelOpen(final @NotNull Claim claim) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getOpenInventory)
                .anyMatch((it) -> it.getTopInventory().getHolder() instanceof ClaimPanel cPanel && cPanel.getClaim().equals(claim) == true);
    }

}
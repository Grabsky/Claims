package cloud.grabsky.claims.panel;

import cloud.grabsky.bedrock.components.ComponentBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.InventoryViewExtensions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

@ExtensionMethod(value = InventoryViewExtensions.class)
public final class ClaimPanel extends Panel {

    @Getter(AccessLevel.PUBLIC)
    public final @NotNull ClaimManager manager;

    @Getter(AccessLevel.PUBLIC)
    private final @Nullable Claim claim;

    @Getter(AccessLevel.PUBLIC)
    private final @Nullable Location accessBlockLocation;

    private ClaimPanel(@NotNull final Component title,
                       final @NotNull InventoryType type,
                       final @Range(from = 0, to = 6) int rows,
                       final @NotNull Consumer<InventoryOpenEvent> onInventoryOpen,
                       final @NotNull Consumer<InventoryCloseEvent> onInventoryClose,
                       final @NotNull Consumer<InventoryClickEvent> onInventoryClick,
                       final @NotNull ClaimManager claimManager,
                       final @Nullable Claim claim,
                       final @Nullable Location accessBlockLocation
    ) {
        super(title, type, rows, onInventoryOpen, onInventoryClose, onInventoryClick);
        // ...
        this.manager = claimManager;
        this.claim = claim;
        this.accessBlockLocation = accessBlockLocation;
    }

    public Player getViewer() {
        return (Player) this.getInventory().getViewers().get(0);
    }

    public void applyClaimTemplate(final @NotNull Consumer<ClaimPanel> template, final boolean clearCurrent) {
        // Clearing inventory before applying template (if requested)
        if (clearCurrent == true)
            this.clear();
        // Applying the template.
        template.accept(this);
    }


    public void updateTitle(final @NotNull Component title) {
        final ClaimPlayer editor = manager.getClaimPlayer(this.getViewer());
        // Setting the title. '*' is appended when modifying not-self-owned claim.
        if (editor.toPlayer().getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel)
            editor.toPlayer().getOpenInventory().title(
                    (claim != null && editor.isOwnerOf(claim) == false)
                            ? ComponentBuilder.of(ComponentBuilder.EMPTY).appendTranslation("ui.util.blank.10").append(title).appendTranslation("ui.util.blank.173").appendTranslation("ui.claims.not_owner").build()
                            : ComponentBuilder.of(ComponentBuilder.EMPTY).appendTranslation("ui.util.blank.10").append(title).build()
            );
    }


    public static final Component INVENTORY_TITLE = text("\u7000\u7100", NamedTextColor.WHITE);

    public static final class Builder extends Panel.Builder<ClaimPanel> {

        private ClaimManager claimManager;
        private Claim claim;
        private Location accessBlockLocation;

        public @NotNull Builder setClaimManager(final @NotNull ClaimManager claimManager) {
            this.claimManager = claimManager; return this.self();
        }

        public @NotNull Builder setClaim(final @Nullable Claim claim) {
            this.claim = claim; return this.self();
        }

        public @NotNull Builder setAccessBlockLocation(final @Nullable Location accessBlockLocation) {
            this.accessBlockLocation = accessBlockLocation; return this.self();
        }

        @Override
        protected @NotNull Builder self() {
            return this;
        }

        @Override
        public @NotNull ClaimPanel build() {
            return new ClaimPanel(INVENTORY_TITLE, InventoryType.CHEST, 6,
                    // OPEN ACTION
                    (event) -> {
                        // Cancelling the even in case other Player is currently viewing this inventory instance.
                        if (event.getInventory().getViewers().size() != 1)
                            event.setCancelled(true);
                        // Cancelling sessions. See comments in Session.Listener.class for more details.
                        Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(event.getPlayer().getUniqueId());
                        // Removing title that may (or may not) be associated with the session.
                        event.getPlayer().clearTitle();
                        // Changing edit state of associated Claim, if present.
                        if (claim != null)
                            claim.setBeingEdited(true);
                    },
                    // CLOSE ACTION
                    (event) -> {
                        // Changing edit state of associated Claim, if present.
                        if (claim != null && claim.isPendingRename() == false)
                            claim.setBeingEdited(false);
                    },
                    // CLICK ACTION
                    (event) -> {
                        // Playing interface click sound, if set.
                        if (PluginConfig.CLAIMS_SETTINGS_UI_CLICK_SOUND != null)
                            event.getWhoClicked().playSound(PluginConfig.CLAIMS_SETTINGS_UI_CLICK_SOUND);
                    },
                    claimManager,
                    claim,
                    accessBlockLocation
            );
        }
    }

    public static boolean isClaimPanel(final @NotNull InventoryView view) {
        return view.getTopInventory().getHolder() instanceof ClaimPanel;
    }

    // TO-DO: Replace with Claim#isBeingEdited check, but first make sure it works properly.
    public static boolean isClaimPanelOpen(final @NotNull Claim claim) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getOpenInventory)
                .anyMatch((it) -> it.getTopInventory().getHolder() instanceof ClaimPanel cPanel && cPanel.getClaim().equals(claim) == true);
    }

}
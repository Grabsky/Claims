package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.claims.util.Iterators.moveIterator;
import static net.kyori.adventure.text.Component.text;

// TO-DO: Clean up the mess.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrowseRelativeClaims implements Consumer<ClaimPanel> {
    /* SINGLETON */ public static final BrowseRelativeClaims INSTANCE = new BrowseRelativeClaims();

    private List<Claim> claims;

    private static final Component INVENTORY_TITLE = text("\u7000\u7302", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    @Override
    public void accept(final @NotNull ClaimPanel cPanel) {
        final Player viewer = (Player) cPanel.getInventory().getViewers().get(0);
        // ...
        this.claims = cPanel.getManager().getClaimPlayer(viewer).getRelativeClaims().stream().toList();
        // ...
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // ...
        this.renderWaypoints(cPanel, viewer, 1, UI_SLOTS.size());
    }

    public void renderWaypoints(final @NotNull ClaimPanel cPanel, final Player viewer, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final var slotsIterator = UI_SLOTS.iterator();
        final var claimsIterator = moveIterator(claims.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        // ...
        this.renderCommonButtons(cPanel);
        // Rendering PREVIOUS PAGE button.
        if (claimsIterator.hasPrevious() == true)
            cPanel.setItem(28, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> renderWaypoints(cPanel, viewer, pageToDisplay - 1, maxOnPage));
        // Rendering waypoints.
        while (claimsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Claim claim = claimsIterator.next();
            final Location location = claim.getHome();
            // ...
            final int slot = slotsIterator.next();
            // ...
            final ItemBuilder icon = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_OWNED_CLAIM);
            // ...
            final @Nullable List<Component> lore = icon.getMeta().lore();
            if (lore != null)
                icon.getMeta().lore(lore.stream().map(line -> {
                    return Message.of(line)
                               .replace("[OWNER]", requirePresent(claim.getOwners().get(0).toUser().getName(), "Unknown"))
                               .replace("[LOCATION]", location.blockX() + ", " + location.blockY() + ", " + location.blockZ())
                               .getMessage();
                }).toList());
            // ...
            if (claim.getType().getUpgradeButton().getItemMeta() instanceof SkullMeta upgradeSkullMeta) {
                icon.edit(SkullMeta.class, (meta) -> {
                    meta.setPlayerProfile(upgradeSkullMeta.getPlayerProfile());
                });
            }
            // ...
            cPanel.setItem(slot, icon.build(), (event) -> {
                // ...
                viewer.teleportAsync(location.add(0.0, 0.5, 0.0), TeleportCause.PLUGIN);
                //Message.of("Teleported to claim " + claim.getId()).send(viewer);
            });
        }
        // Rendering NEXT PAGE button.
        if (claimsIterator.hasNext() == true)
            cPanel.setItem(34, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> renderWaypoints(cPanel, viewer, pageToDisplay + 1, maxOnPage));
    }

    private void renderCommonButtons(final ClaimPanel cPanel) {
        cPanel.setItem(10, new ItemStack(PluginItems.INTERFACE_FUNCTIONAL_ICON_SPAWN), null);
        cPanel.setItem(12, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_WAYPOINTS), (event) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
        cPanel.setItem(14, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_OWNED_CLAIMS), (event) -> cPanel.applyClaimTemplate(BrowseOwnedClaims.INSTANCE, true));
        cPanel.setItem(16, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_RELATIVE_CLAIMS), null);
        // RETURN
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> {
            if (cPanel.getClaim() != null) {
                cPanel.applyTemplate(BrowseCategories.INSTANCE, true);
                return;
            }
            cPanel.close();
        });
    }

}

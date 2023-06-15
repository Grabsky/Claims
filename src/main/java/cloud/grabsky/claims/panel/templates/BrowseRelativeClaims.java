package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.util.Utilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static java.util.Comparator.comparingLong;
import static net.kyori.adventure.text.Component.text;

// TO-DO: Clean up the mess.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrowseRelativeClaims implements Consumer<ClaimPanel> {
    /* SINGLETON */ public static final BrowseRelativeClaims INSTANCE = new BrowseRelativeClaims();

    private List<Claim> claims;

    private static final Component INVENTORY_TITLE = text("\u7000\u7302", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm");

    @Override
    public void accept(final @NotNull ClaimPanel cPanel) {
        final Player viewer = (Player) cPanel.getInventory().getViewers().get(0);
        // ...
        this.claims = cPanel.getManager().getClaimPlayer(viewer).getRelativeClaims().stream()
                .sorted(comparingLong(claim -> requirePresent(claim.getCreatedOn(), 0L)))
                .toList();
        // ...
        cPanel.updateTitle(INVENTORY_TITLE);
        // ...
        this.render(cPanel, viewer, 1, UI_SLOTS.size());
    }

    public void render(final @NotNull ClaimPanel cPanel, final Player viewer, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final var slotsIterator = UI_SLOTS.iterator();
        final var claimsIterator = moveIterator(claims.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        // ...
        this.renderCommonButtons(cPanel);
        // Rendering PREVIOUS PAGE button.
        if (claimsIterator.hasPrevious() == true)
            cPanel.setItem(28, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> render(cPanel, viewer, pageToDisplay - 1, maxOnPage));
        // Rendering waypoints.
        while (claimsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Claim claim = claimsIterator.next();
            final Location location = claim.getHome();
            // ...
            final int slot = slotsIterator.next();
            // ...
            final ItemBuilder icon = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_RELATIVE_CLAIM);
            // ...
            final @Nullable List<Component> lore = icon.getMeta().lore();
            if (lore != null) {
                final Long createdOn = claim.getCreatedOn();
                icon.getMeta().lore(lore.stream().map(line -> {
                    return Message.of(line)
                            .replace("[OWNER]", requirePresent(claim.getOwners().get(0).toUser().getName(), "N/A"))
                            .replace("[LOCATION]", location.blockX() + ", " + location.blockY() + ", " + location.blockZ())
                            .replace("[CREATED_ON]", (createdOn != null) ? DATE_FORMAT.format(new Date(createdOn)) : "N/A")
                            .getMessage();
                }).toList());
            }
            // ...
            if (claim.getType().getUpgradeButton().getItemMeta() instanceof SkullMeta upgradeSkullMeta) {
                icon.edit(SkullMeta.class, (meta) -> {
                    meta.setPlayerProfile(upgradeSkullMeta.getPlayerProfile());
                });
            }
            // ...
            cPanel.setItem(slot, icon.build(), (event) -> {
                // Closing the panel.
                cPanel.close();
                // Teleporting...
                Utilities.teleport(viewer, location, PluginConfig.CLAIM_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                    // Displaying particles. NOTE: This can expose vanished players.
                    if (PluginConfig.CLAIM_SETTINGS_TELEPORT_EFFECTS != null) {
                        PluginConfig.CLAIM_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                            location.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                        });
                    }
                    // Playing sounds. NOTE: This can expose vanished players.
                    if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                        old.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                    if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN != null)
                        current.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
                });
            });
        }
        // Rendering NEXT PAGE button.
        if (claimsIterator.hasNext() == true)
            cPanel.setItem(34, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> render(cPanel, viewer, pageToDisplay + 1, maxOnPage));
    }

    private void renderCommonButtons(final ClaimPanel cPanel) {
        cPanel.setItem(10, new ItemStack(PluginItems.INTERFACE_FUNCTIONAL_ICON_SPAWN), (event) -> {
            // Closing the panel.
            cPanel.close();
            // ...
            final Player viewer = cPanel.getViewer();
            final Location location = AzureProvider.getAPI().getWorldManager().getSpawnPoint(PluginConfig.DEFAULT_WORLD);
            // Teleporting...
            Utilities.teleport(viewer, location, PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                // Displaying particles. NOTE: This can expose vanished players.
                if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS != null) {
                    PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                        location.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                    });
                }
                // Playing sounds. NOTE: This can expose vanished players.
                if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                    old.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN != null)
                    current.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
            });
        });
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

package cloud.grabsky.claims.panel.views;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public final class ViewHomes extends ClaimPanel.View {

    private final ClaimManager manager = Claims.getInstance().getClaimManager();

    @Override
    public void accept(final ClaimPanel panel) {
        final ClaimPlayer owner = panel.getClaim().getOwner();
        final Player viewer = panel.getViewer();
        // Changing panel texture
        panel.updateClientTitle(text("\u7000\u7103", NamedTextColor.WHITE));
        // Generating the view
        this.generateView(panel, 1);
    }

    private void generateView(final ClaimPanel panel, final int pageToDisplay) {
        final Player viewer = panel.getViewer();
        panel.clear();
        // Calculating index
        int index = 0;
        // For each 'use-able' slot (10 - first slot, 35 - last slot)
        for (final Claim claim : panel.getClaim().getOwner().getRelativeClaims()) {
            final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            item.editMeta(meta -> {
                meta.setDisplayName(claim.getOwner().toString());
            });
            panel.setItem(index, item, (event) -> {
                viewer.closeInventory();
                PaperLib.teleportAsync(viewer, claim.getHome());
            });
            index++;
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, PluginItems.NAVIGATION_PREVIOUS, (event) -> generateView(panel, pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= 1) panel.setItem(26, PluginItems.NAVIGATION_NEXT, (event) -> generateView(panel, pageToDisplay + 1));
        // Return button
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> {
            if (panel.getClaim().getOwner().hasClaim() == true) {
                panel.applyView(new ViewMain(), true);
                return;
            }
            viewer.closeInventory();
        });
    }
}

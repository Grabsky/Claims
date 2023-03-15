package cloud.grabsky.claims.panel.sections;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.panel.Panel;
import cloud.grabsky.claims.templates.Icons;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import me.grabsky.indigo.utils.Teleport;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SectionHomes extends Section {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private final Player viewer;
    private final ClaimPlayer claimOwner;
    private List<String> relatives;
    private int maxOnPage;
    private int usableSize;
    private int pages;

    public SectionHomes(Panel panel) {
        super(panel);
        this.viewer = panel.getViewer();
        this.claimOwner = panel.getClaimOwner();
    }

    public void prepare() {
        this.relatives = (viewer.getUniqueId().equals(claimOwner.getUniqueId()) && viewer.hasPermission("claims.plugin.displayallclaims")) ? new ArrayList<>(manager.getClaimIds()) : new ArrayList<>(manager.getClaimPlayer(claimOwner.getUniqueId()).getRelatives());
        // Removing player's claim from a list (it's there only when displaying all claims)
        if (claimOwner.hasClaim()) {
            this.relatives.removeIf((id) -> id.equals(claimOwner.getClaim().getId()));
        }
        this.maxOnPage = 21;
        this.usableSize = relatives.size();
        this.pages = (relatives.size() - 1) / maxOnPage + 1;
    }

    @Override
    public void apply() {
        // Changing panel texture
        panel.updateClientTitle("§f\u7000\u7103");
        // Generating the view
        this.generateView(1);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Calculating index
        int index = (pageToDisplay * maxOnPage) - maxOnPage;
        // For each 'use-able' slot (10 - first slot, 35 - last slot)
        for (int slot = 10; slot < 35; slot++, index++) {
            // Making sure we didn't run out of index
            if (index >= usableSize) break;
            // Skipping border slots
            if ((slot + 1) % 9 == 0) slot += 2;
            // Getting claim for current index
            final Claim relativeClaim = manager.getClaim(relatives.get(index));
            // Returning if claim with given id is not found
            if (relativeClaim == null) continue;
            // Getting owner of claim
            final User user = UserCache.get(relativeClaim.getOwner());
            // Setting teleport item
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + user.getName())
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullTexture(user.getTexture())
                    .build(), (event) -> {
                        viewer.closeInventory();
                        Teleport.async(viewer, relativeClaim.getHome(), ClaimsConfig.TELEPORT_DELAY, "azure.bypass.teleportdelay");
            });
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, Icons.NAVIGATION_PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= pages) panel.setItem(26, Icons.NAVIGATION_NEXT, (event) -> generateView(pageToDisplay + 1));
        // Return button
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> {
            if (claimOwner.hasClaim()) {
                panel.applySection(new SectionMain(panel));
                return;
            }
            viewer.closeInventory();
        });
    }
}

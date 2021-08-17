package me.grabsky.claims.panel.sections;

import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.configuration.Items;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.utils.InventoryUtils;
import me.grabsky.claims.utils.TeleportUtils;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SectionHomes extends Section {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private List<String> relatives;
    private int maxOnPage;
    private int usableSize;
    private int pages;

    public SectionHomes(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    public void prepare() {
        // Some useful values
        this.relatives = (executor.getUniqueId().equals(owner) && executor.hasPermission("skydistrict.plugin.claims.showall")) ? manager.getClaimIds() : new ArrayList<>(manager.getClaimPlayer(owner).getRelatives());
        this.maxOnPage = 21;
        this.usableSize = relatives.size();
        this.pages = (relatives.size() - 1) / maxOnPage + 1;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(executor, "§f\u7000\u7103", editMode);
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
                        executor.closeInventory();
                        TeleportUtils.teleportAsync(executor, relativeClaim.getHome(), 5);
            });
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, Items.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= pages) panel.setItem(26, Items.NEXT, (event) -> generateView(pageToDisplay + 1));
        // Return button
        panel.setItem(49, Items.RETURN, (event) -> {
            if (claim != null) {
                panel.applySection(new SectionMain(panel, executor, owner, claim));
            } else {
                executor.closeInventory();
            }
        });
    }
}

package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryUtils;
import net.skydistrict.claims.utils.TeleportUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

// I feel like this is still something that have to be worked on.
// Current way of displaying claims to player is very unclear and not intuitive at all.
// Still thinking what the most user-friendly approach would look like.
public class SectionHomes extends Section {
    private final ClaimManager manager = Claims.getInstance().getClaimManager();
    private boolean hasRegion = false;
    private ItemStack home;
    private String[] relatives;
    private int length;
    private int maxOnPage;

    public SectionHomes(Panel panel, Player executor, UUID owner) {
        super(panel, executor, owner);
    }

    public SectionHomes(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
        this.hasRegion = true;
    }

    public void prepare() {
        this.home = (hasRegion) ? Items.HOME : Items.HOME_DISABLED;
        // Some useful values
        this.relatives = manager.getClaimPlayer(owner).getRelatives().toArray(new String[0]);
        this.length = relatives.length;
        this.maxOnPage = 4;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(executor, "§f\u7000\u7106", editMode);
        // Generating the view
        this.generateView(1);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Displaying owned claim
        panel.setItem(10, this.home, (event) -> {
            if (hasRegion) {
                executor.closeInventory();
                TeleportUtils.teleportAsync(executor, claim.getHome(), 5);
            }
        });
        // Displaying regions player have access to
        int startFrom = ((pageToDisplay * maxOnPage) - maxOnPage);
        int slot = 13, lastIndex = 0;
        for (int index = startFrom; index < length; index++) {
            if (slot == maxOnPage) {
                lastIndex = index;
                break;
            }
            final Claim relativeClaim = manager.getClaim(relatives[index]);
            if (relativeClaim == null) continue;
            final User user = UserCache.get(relativeClaim.getOwner());
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + user.getName())
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullTexture(user.getTexture())
                    .build(), (event) -> {
                        executor.closeInventory();
                        TeleportUtils.teleportAsync(executor, relativeClaim.getHome(), 5);
            });
            startFrom++;
            lastIndex++;
        }

        // Navigation buttons
        if (lastIndex > maxOnPage) panel.setItem(12, Items.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        if (lastIndex < length) panel.setItem(17, Items.NEXT, (event) -> generateView(pageToDisplay + 1));
        // Return button
        panel.setItem(49, Items.RETURN, (event) -> {
            if (hasRegion) {
                panel.applySection(new SectionMain(panel, executor, owner, claim));
            } else {
                executor.closeInventory();
            }
        });
    }
}

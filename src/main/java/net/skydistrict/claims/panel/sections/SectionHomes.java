package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.api.ClaimsAPI;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import net.skydistrict.claims.utils.TeleportH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionHomes extends Section {
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
        this.relatives = ClaimsAPI.getClaimPlayer(owner).getRelatives().toArray(new String[0]);
        this.length = relatives.length;
        this.maxOnPage = 4;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7106", editMode);
        // Generating the view
        this.generateView(1);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Displaying owned claim
        panel.setItem(10, this.home, (event) -> {
            if (hasRegion) {
                executor.closeInventory();
                if (executor.hasPermission("skydistrict.claims.bypass.teleportdelay")) {
                    TeleportH.teleportAsync(executor, claim.getHome(), 0);
                    return;
                }
                Lang.send(executor, Lang.TELEPORTING, Config.TELEPORT_DELAY);
                TeleportH.teleportAsync(executor, claim.getHome(), 5);
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
            Claim relativeClaim = ClaimsAPI.getClaim(relatives[index]);
            if (relativeClaim == null) continue;
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + UUIDCache.get(relativeClaim.getOwner()))
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullOwner(relativeClaim.getOwner())
                    .build(), (event) -> {
                        executor.closeInventory();
                        if (executor.hasPermission("skydistrict.claims.bypass.teleportdelay")) {
                            TeleportH.teleportAsync(executor, relativeClaim.getHome(), 0);
                            return;
                        }
                        Lang.send(executor, Lang.TELEPORTING, Config.TELEPORT_DELAY);
                        TeleportH.teleportAsync(executor, relativeClaim.getHome(), 5);
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

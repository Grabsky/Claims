package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.api.ClaimsAPI;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.Claim;
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

    @Override
    public void prepare() {
        if (hasRegion) {
            this.home = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§lTeren")
                    .setLore("§7Kliknij, aby się teleportować.")
                    .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzk4MzQ2ZWY3ZjZhYmJhZmUxYjU0ZWQ0NmExNzc3OWNmODMyN2YzNTNjYzQxMDU1ZjFjNmNkYTA4OTQ1MzZmZSJ9fX0=")
                    .build();
        } else {
            this.home =  new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§7§lTeren")
                    .setLore("§7Nie znaleziono terenu.")
                    .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE3M2U5YjIxNjE3OTBlNzFhMTg3ZDI1YjkyOGY2MmIyMmQ2YjM1N2MyMjYzN2Y5OGVhNjEwNDRjNjdjNjMwMCJ9fX0=")
                    .build();
        }
        // Some useful values
        this.relatives = ClaimsAPI.getClaimPlayer(owner).getRelatives().toArray(new String[0]);
        this.length = relatives.length;
        this.maxOnPage = 5;
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
        // Teleport to owned claim button
        panel.setItem(13, this.home, (event) -> {
            if (hasRegion) {
                executor.closeInventory();
                executor.sendMessage(Lang.TELEPORTING);
                TeleportH.teleportAsync(executor, claim.getHome(), 5);
            }
        });
        // Displaying regions player have access to
        int startFrom = ((pageToDisplay * maxOnPage) - maxOnPage);
        int slot = 29, lastIndex = 0;
        for (int index = startFrom; index < length; index++) {
            if (slot == maxOnPage) {
                lastIndex = index;
                break;
            }
            Claim claim = ClaimsAPI.getClaim(relatives[index]);
            if (claim == null) continue;
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + UUIDCache.get(claim.getOwner()))
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullOwner(claim.getOwner())
                    .build(), (event) -> {
                executor.closeInventory();
                executor.sendMessage(Lang.TELEPORTING);
                TeleportH.teleportAsync(executor, claim.getHome(), 5);
            });
            startFrom++;
            lastIndex++;
        }

        // Navigation buttons
        if (lastIndex > maxOnPage) panel.setItem(28, Items.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        if (lastIndex < length) panel.setItem(34, Items.NEXT, (event) -> generateView(pageToDisplay + 1));
        // Return button
        panel.setItem(49, Items.RETURN, (event) -> {
            if (hasRegion) panel.applySection(new SectionMain(panel, executor, owner, claim));
            else executor.closeInventory();
        });
    }
}

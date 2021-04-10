package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.Lang;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import net.skydistrict.claimsgui.utils.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class SectionHomes extends Section {
    private boolean hasRegion = false;
    private ItemStack home;

    public SectionHomes(Panel panel, Player executor, UUID owner) {
        super(panel, executor, owner);
    }

    public SectionHomes(Panel panel, Player player, UUID owner, PSRegion region) {
        super(panel, player, owner, region);
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
            return;
        }
        this.home =  new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§7§lTeren")
                .setLore("§7Nie znaleziono terenu.")
                .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE3M2U5YjIxNjE3OTBlNzFhMTg3ZDI1YjkyOGY2MmIyMmQ2YjM1N2MyMjYzN2Y5OGVhNjEwNDRjNjdjNjMwMCJ9fX0=")
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(executor, "§f\u7000\u7006");
        // Generating the view
        this.generateView(1, 5, PSPlayer.fromUUID(owner).getPSRegions(Bukkit.getWorlds().get(0), true));
    }

    private void generateView(int pageToDisplay, int maxOnPage, List<PSRegion> regions) {
        panel.clear();
        // Teleport to owned claim button
        panel.setItem(13, this.home, (event) -> {
            if (hasRegion) {
                executor.closeInventory();
                executor.sendMessage(Lang.TELEPORTING);
                Teleport.asyncTeleport(executor, region.getHome(), 5);
            }
        });
        // Displaying specific page
        final int size = regions.size();
        final int pages = (size - 1) / maxOnPage + 1;
        // For each "use-able" slot
        for (int slot = 29, index = 0 ; index < maxOnPage; slot++) {

            // Getting 'fixed' index
            int fixedIndex = ((pageToDisplay * maxOnPage) - maxOnPage) + index;
            if (fixedIndex >= size) break;
            PSRegion reg = regions.get(fixedIndex);

            if (reg.getOwners().contains(owner)) {
                slot--;
                index++;
                continue;
            }
            // Adding skull to GUI
            UUID ownerUuid = reg.getOwners().get(0);
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + UUIDCache.getNameFromUUID(ownerUuid))
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullOwner(ownerUuid)
                    .build(), (event) -> {
                executor.closeInventory();
                executor.sendMessage(Lang.TELEPORTING);
                Teleport.asyncTeleport(executor, reg.getHome(), 5);
            });
            index++;
        }
        // If player is not on the first page, display PREVIOUS PAGE button
        if (pageToDisplay > 1) {
            panel.setItem(29, StaticItems.PREVIOUS, (event) -> generateView(pageToDisplay - 1, maxOnPage, regions));
        }
        // If there is more pages, display NEXT PAGE button
        if (pageToDisplay + 1 <= pages) {
            panel.setItem(34, StaticItems.NEXT, (event) -> generateView(pageToDisplay + 1, maxOnPage, regions));
        }
        // Displaying return button
        panel.setItem(49, StaticItems.RETURN, (event) -> {
            if (hasRegion) {
                panel.applySection(new SectionMain(panel, executor, owner, region));
            } else {
                executor.closeInventory();
            }
        });
    }
}

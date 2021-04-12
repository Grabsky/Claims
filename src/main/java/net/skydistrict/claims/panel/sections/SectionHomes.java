package net.skydistrict.claims.panel.sections;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import net.skydistrict.claims.utils.TeleportH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class SectionHomes extends Section {
    private boolean hasRegion = false;
    private ItemStack home;
    private PSRegion[] regions;
    private int length;
    private int maxOnPage;

    public SectionHomes(Panel panel, Player executor, UUID owner) {
        super(panel, executor, owner);
    }

    public SectionHomes(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
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

        // Filtering regions list
        this.regions = new PSRegion[0];
        final List<PSRegion> regionsRaw = PSPlayer.fromUUID(owner).getPSRegions(Config.DEFAULT_WORLD, true);
        PSPlayer.fromUUID(owner).getHomes(Config.DEFAULT_WORLD);
        for (int i = 0; i < regionsRaw.size(); i++) {
            PSRegion region = regionsRaw.get(i);
            if (region.isOwner(owner)) regions[i] = regions[i] = region;
        }

        // Some useful values
        this.length = regions.length;
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
                TeleportH.asyncTeleport(executor, region.getHome(), 5);
            }
        });

        // Displaying regions player have access to
        int startFrom = ((pageToDisplay * this.maxOnPage) - this.maxOnPage);
        int slot = 29, lastIndex = 0;
        for (int index = startFrom; index < this.length; index++) {
            if (slot == this.maxOnPage) {
                lastIndex = index;
                break;
            }

            final PSRegion reg = this.regions[index];
            final UUID ownerUuid = reg.getOwners().get(0);

            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e§l" + UUIDCache.getNameFromUUID(ownerUuid))
                    .setLore("§7Kliknij, aby teleportować się", "§7na teren tego gracza.")
                    .setSkullOwner(ownerUuid)
                    .build(), (event) -> {
                executor.closeInventory();
                executor.sendMessage(Lang.TELEPORTING);
                TeleportH.asyncTeleport(executor, reg.getHome(), 5);
            });
            slot++;
        }

        // Navigation buttons
        if (lastIndex > this.maxOnPage) panel.setItem(28, StaticItems.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        if (lastIndex < this.length) panel.setItem(34, StaticItems.NEXT, (event) -> generateView(pageToDisplay + 1));
        // Return button
        panel.setItem(49, StaticItems.RETURN, (event) -> {
            if (hasRegion) {
                panel.applySection(new SectionMain(panel, executor, owner, region));
            } else {
                executor.closeInventory();
            }
        });
    }
}

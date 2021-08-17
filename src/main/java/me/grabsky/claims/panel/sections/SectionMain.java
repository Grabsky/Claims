package me.grabsky.claims.panel.sections;

import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.configuration.Items;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.utils.InventoryUtils;
import me.grabsky.claims.utils.TeleportUtils;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SectionMain extends Section {
    private ItemStack members;

    public SectionMain(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        this.members = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullTexture(UserCache.get(owner).getTexture())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(executor, "§f\u7000\u7101", editMode);
        // Setting menu items
        panel.setItem(11, Items.HOMES, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    executor.closeInventory();
                    TeleportUtils.teleportAsync(executor, claim.getHome(), 5);
                }
                case RIGHT, SHIFT_RIGHT -> panel.applySection(new SectionHomes(panel, executor, owner, claim));
            }
        });
        panel.setItem(13, members, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, claim)));
        panel.setItem(15, Items.SETTINGS, (event) -> panel.applySection(new SectionSettings(panel, executor, owner, claim)));
        panel.setItem(49, Items.RETURN, (event) -> executor.closeInventory());
    }
}

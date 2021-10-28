package me.grabsky.claims.panel.sections;

import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Icons;
import me.grabsky.claims.utils.InventoryUtils;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import me.grabsky.indigo.utils.Components;
import me.grabsky.indigo.utils.Teleport;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SectionMain extends Section {
    public static Component INVENTORY_TITLE = Components.parseSection("§f\u7000\u7100");
    private ItemStack membersIcon;

    public SectionMain(final Panel panel, final Player viewer, final UUID claimOwnerUniqueId, @Nullable Claim claim) {
        super(panel, viewer, claimOwnerUniqueId, claim);
    }

    @Override
    public void prepare() {
        this.membersIcon = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullTexture(UserCache.get(claimOwnerUniqueId).getTexture())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(viewer, "§f\u7000\u7101", editMode);
        // Setting menu items
        panel.setItem(11, Icons.CATEGORY_HOMES, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    viewer.closeInventory();
                    Teleport.async(viewer, claim.getHome(), ClaimsConfig.TELEPORT_DELAY, "azure.bypass.teleportdelay");
                }
                case RIGHT, SHIFT_RIGHT -> panel.applySection(new SectionHomes(panel, viewer, claimOwnerUniqueId, claim));
            }
        });
        panel.setItem(13, membersIcon, (event) -> panel.applySection(new SectionMembers(panel, viewer, claimOwnerUniqueId, claim)));
        panel.setItem(15, Icons.CATEGORY_SETTINGS, (event) -> panel.applySection(new SectionSettings(panel, viewer, claimOwnerUniqueId, claim)));
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }
}

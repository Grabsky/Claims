package me.grabsky.claims.panel.sections;

import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Icons;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.user.UserCache;
import me.grabsky.indigo.utils.Components;
import me.grabsky.indigo.utils.Teleport;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SectionMain extends Section {
    private ItemStack membersIcon;
    private final Player viewer;
    private final ClaimPlayer claimOwner;

    public static Component INVENTORY_TITLE = Components.parseSection("§f\u7000\u7100");

    public SectionMain(final Panel panel) {
        super(panel);
        this.viewer = panel.getViewer();
        this.claimOwner = panel.getClaimOwner();
    }

    @Override
    public void prepare() {
        this.membersIcon = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lCzłonkowie")
                .setLore("§7Kliknij, aby zarządzać dodanymi do terenu.")
                .setSkullTexture(UserCache.get(claimOwner.getUniqueId()).getTexture())
                .build();
    }

    @Override
    public void apply() {
        // Changing panel texture
        panel.updateClientTitle("§f\u7000\u7101");
        // Setting menu items
        panel.setItem(11, Icons.CATEGORY_HOMES, (event) -> {
            switch (event.getClick()) {
                case LEFT, SHIFT_LEFT -> {
                    viewer.closeInventory();
                    Teleport.async(viewer, claimOwner.getClaim().getHome(), ClaimsConfig.TELEPORT_DELAY, "azure.bypass.teleportdelay");
                }
                case RIGHT, SHIFT_RIGHT -> panel.applySection(new SectionHomes(panel));
            }
        });
        panel.setItem(13, membersIcon, (event) -> panel.applySection(new SectionMembers(panel)));
        panel.setItem(15, Icons.CATEGORY_SETTINGS, (event) -> panel.applySection(new SectionSettings(panel)));
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> viewer.closeInventory());
    }
}

package me.grabsky.claims.panel.sections;

import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Icons;
import me.grabsky.claims.utils.InventoryUtils;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SectionMembers extends Section {
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();

    public SectionMembers(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        // Nothing to prepare
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(viewer, "§f\u7000\u7104", editMode);
        // Generating the view
        this.generateView();
    }

    private void generateView() {
        panel.clear();
        // For each added member slot
        int slot = 11;
        for (UUID uuid : claim.getMembers()) {
            // Add skull to gui
            final User user = UserCache.get(uuid);
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§c§l" + user.getName())
                    .setLore("§7Kliknij, aby §cwyrzucić§7 z terenu.")
                    .setSkullTexture(user.getTexture())
                    .build(), event -> {
                // One more check just in case something changed while GUI was open
                if (claim.removeMember(uuid)) {
                    this.generateView();
                    if (ClaimsConfig.LOGS) {
                        fileLogger.log(ClaimsConfig.LOG_FORMAT_MEMBER_REMOVED
                                .replace("{member-name}", user.getName())
                                .replace("{member-uuid}", user.getUniqueId().toString())
                                .replace("{claim-id}", claim.getId())
                                .replace("{claim-level}", String.valueOf(claim.getLevel()))
                                .replace("{issuer-name}", viewer.getName())
                                .replace("{issuer-uuid}", viewer.getUniqueId().toString()));
                    }
                } else {
                    viewer.closeInventory();
                    ClaimsLang.send(viewer, ClaimsLang.NOT_MEMBER);
                }
            });
            slot = (slot == 15) ? 20 : slot + 1;
        }
        if (slot != 25) panel.setItem(slot, Icons.ICON_ADD_MEMBER, event -> panel.applySection(new SectionMembersAdd(panel, viewer, claimOwnerUniqueId, claim)));
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionMain(panel, viewer, claimOwnerUniqueId, claim)));
    }
}

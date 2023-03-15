package cloud.grabsky.claims.panel.sections;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLang;
import cloud.grabsky.claims.panel.Panel;
import cloud.grabsky.claims.templates.Icons;
import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SectionMembers extends Section {
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();
    private final Player viewer;
    private final Claim claim;

    public SectionMembers(Panel panel) {
        super(panel);
        this.viewer = panel.getViewer();
        this.claim = panel.getClaimOwner().getClaim();
    }

    @Override
    public void prepare() {
        // Nothing to prepare
    }

    @Override
    public void apply() {
        // Changing panel texture
        panel.updateClientTitle("§f\u7000\u7104");
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
        if (slot != 25) panel.setItem(slot, Icons.ICON_ADD_MEMBER, event -> panel.applySection(new SectionMembersAdd(panel)));
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionMain(panel)));
    }
}

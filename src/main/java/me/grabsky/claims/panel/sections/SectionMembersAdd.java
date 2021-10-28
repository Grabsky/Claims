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
import me.grabsky.vanish.Vanish;
import me.grabsky.vanish.api.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SectionMembersAdd extends Section {
    private final FileLogger fileLogger = Claims.getInstance().getFileLogger();
    private List<User> onlineUsers;
    private int maxOnPage;
    private int usableSize;
    private int pages;

    public SectionMembersAdd(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        final VanishAPI vanish = (Bukkit.getPluginManager().getPlugin("Vanish") != null) ? Vanish.getInstance().getAPI() : null;
        this.onlineUsers = UserCache.getOnlineUsers().stream()
                .filter(user -> (!user.getUniqueId().equals(claimOwnerUniqueId) && !claim.isMember(user.getUniqueId()) && (vanish == null || !vanish.isVanished(user.getUniqueId()))))
                .collect(Collectors.toList());
        this.maxOnPage = 21;
        this.usableSize = onlineUsers.size();
        this.pages = (onlineUsers.size() - 1) / maxOnPage + 1;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryUtils.updateTitle(viewer, "§f\u7000\u7103", editMode);
        // Display first page of online players
        this.generateView(1);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Calculating index
        int index = (pageToDisplay * maxOnPage) - maxOnPage;
        // For each 'use-able' slot (10 - first slot, 35 - last slot)
        for (int slot = 10; slot < 35; slot++, index++) {
            // Making sure we didn't run out of index
            if (index >= usableSize) break;
            // Skipping border slots
            if ((slot + 1) % 9 == 0) slot += 2;
            final User user = onlineUsers.get(index);
            // Adding skull to GUI
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a§l" + user.getName())
                    .setLore("§7Kliknij, aby §adodać§7 do terenu.")
                    .setSkullTexture(user.getTexture())
                    .build(), (event) -> {
                // One more check just in case something changed while GUI was open
                if (claim.addMember(user.getUniqueId())) {
                    panel.applySection(new SectionMembers(panel, viewer, claimOwnerUniqueId, claim));
                    if (ClaimsConfig.LOGS) {
                        fileLogger.log(ClaimsConfig.LOG_FORMAT_MEMBER_ADDED
                                .replace("{member-name}", user.getName())
                                .replace("{member-uuid}", user.getUniqueId().toString())
                                .replace("{claim-id}", claim.getId())
                                .replace("{claim-level}", String.valueOf(claim.getLevel()))
                                .replace("{issuer-name}", viewer.getName())
                                .replace("{issuer-uuid}", viewer.getUniqueId().toString()));
                    }
                } else {
                    viewer.closeInventory();
                    ClaimsLang.send(viewer, ClaimsLang.REACHED_MEMBERS_LIMIT.replace("{limit}", String.valueOf(ClaimsConfig.MEMBERS_LIMIT)));
                }
            });
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, Icons.NAVIGATION_PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= pages) panel.setItem(26, Icons.NAVIGATION_NEXT, (event) -> generateView(pageToDisplay + 1));
        // As usually, displaying RETURN button
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionMembers(panel, viewer, claimOwnerUniqueId, claim)));
    }
}

package net.skydistrict.claims.panel.sections;

import me.grabsky.indigo.builders.ItemBuilder;
import me.grabsky.indigo.logger.FileLogger;
import me.grabsky.indigo.user.User;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// TO-DO: Replace Bukkit.getOnlinePlayers() with something else
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
        this.onlineUsers = UserCache.getOnlineUsers().stream()
                .filter(user -> (!user.getUniqueId().equals(owner) && !claim.isMember(user.getUniqueId())))
                .collect(Collectors.toList());
        this.maxOnPage = 21;
        this.usableSize = onlineUsers.size();
        this.pages = (onlineUsers.size() - 1) / maxOnPage + 1;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7103", editMode);
        // Display first page of online players
        this.generateView(1);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Calculating index
        int index = (pageToDisplay * maxOnPage) - maxOnPage;
        // For each 'use-able' slot (10 - first slot, 35 - last slot)
        for (int slot = 10; slot < 35; slot++, index++) {
            // Making sure we didn't ran out of index
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
                    panel.applySection(new SectionMembers(panel, executor, owner, claim));
                    if (Config.LOGS) {
                        fileLogger.log(new StringBuilder().append("MEMBER_ADDED | ")
                                .append(claim.getId()).append(" | ")
                                .append(executor.getName()).append(" (").append(executor.getUniqueId()).append(") | ")
                                .append(user.getName()).append(" (").append(user.getUniqueId()).append(")")
                                .toString());
                    }
                } else {
                    executor.closeInventory();
                    Lang.send(executor, Lang.REACHED_MEMBERS_LIMIT.replace("%limit%", String.valueOf(Config.MEMBERS_LIMIT)));
                }
            });
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, Items.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= pages) panel.setItem(26, Items.NEXT, (event) -> generateView(pageToDisplay + 1));
        // As usually, displaying RETURN button
        panel.setItem(49, Items.RETURN, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, claim)));
    }
}

package net.skydistrict.claims.panel.sections;

import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// TO-DO: Replace Bukkit.getOnlinePlayers() with better method
public class SectionMembersAdd extends Section {
    private List<Player> onlinePlayers;
    private int maxOnPage;
    private int usableSize;
    private int pages;

    public SectionMembersAdd(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
    }

    @Override
    public void prepare() {
        this.onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player -> (!player.getUniqueId().equals(owner) && !claim.isMember(player.getUniqueId())))
                .collect(Collectors.toList());
        this.maxOnPage = 21;
        this.usableSize = onlinePlayers.size();
        this.pages = (onlinePlayers.size() - 1) / maxOnPage + 1;
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
            final Player player = onlinePlayers.get(index);
            // Adding skull to GUI
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a§l" + player.getName())
                    .setLore("§7Kliknij, aby §adodać§7 do terenu.")
                    .setSkullOwner(player.getUniqueId())
                    .build(), (event) -> {
                // One more check just in case something changed while GUI was open
                if (claim.addMember(player.getUniqueId())) panel.applySection(new SectionMembers(panel, executor, owner, claim));
                else {
                    executor.closeInventory();
                    executor.sendMessage(Lang.REACHED_MEMBERS_LIMIT);
                }
            });
        }
        // If player is not on the first page - displaying PREVIOUS PAGE button
        if (pageToDisplay > 1) panel.setItem(18, StaticItems.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        // If there is more pages - displaying NEXT PAGE button
        if (pageToDisplay + 1 <= pages) panel.setItem(26, StaticItems.NEXT, (event) -> generateView(pageToDisplay + 1));
        // As usually, displaying RETURN button
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, claim)));
    }
}

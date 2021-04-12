package net.skydistrict.claims.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// TO-DO: Vanish support (future task)
public class SectionMembersAdd extends Section {
    private List<Player> players;
    private int size;
    private int maxOnPage;
    private int pages;

    public SectionMembersAdd(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
    }

    @Override
    public void prepare() {
        ArrayList<UUID> members = region.getMembers();
        ArrayList<Player> p = new ArrayList<>(60);
        for (int i = 0; i < 60; i++) {
            p.add(Bukkit.getPlayer("Grabsky"));
        }
        long s1 = System.nanoTime();
        this.players = p.stream()
                .filter(player -> (player != executor) && !region.isMember(player.getUniqueId()))
                .collect(Collectors.toList());
        System.out.println((System.nanoTime() - s1) / 1000000D);
        this.size = players.size();
        this.maxOnPage = 21;
        this.pages = (size - 1) / maxOnPage + 1;
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7103", editMode);
        // Display first page of online players
        long s1 = System.nanoTime();
        this.generateView(1);
        System.out.println((System.nanoTime() - s1) / 1000000D);
    }

    private void generateView(int pageToDisplay) {
        panel.clear();
        // Updating list of members
        final ArrayList<UUID> members = region.getMembers();
        // Calculating number of pages and range
        // For each "use-able" slot
        for (int slot = 9, index = 0 ; index < this.maxOnPage; slot++) {
            if ((slot + 1) % 9 == 0 || slot % 9 == 0) continue;
            // Getting 'fixed' index
		    int fixedIndex = ((pageToDisplay * this.maxOnPage) - this.maxOnPage) + index;
            if (fixedIndex >= this.size) break;
            Player player = this.players.get(fixedIndex);
            UUID uuid = player.getUniqueId();
            // Add skull to gui
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a§l" + player.getName())
                    .setLore("§7Kliknij, aby §adodać§7 do terenu.")
                    .setSkullOwner(uuid)
                    .build(), (event) -> {
                // One more check just in case something changed while GUI was open
                if (region.getMembers().size() < 10) {
                    region.addMember(uuid);
                    panel.applySection(new SectionMembers(panel, executor, owner, region));
                } else {
                    executor.closeInventory();
                    executor.sendMessage(Lang.REACHED_MEMBERS_LIMIT);
                }
            });
            index++;
        }
        // If player is not on the first page, display PREVIOUS PAGE button
        if (pageToDisplay > 1) {
            panel.setItem(18, StaticItems.PREVIOUS, (event) -> generateView(pageToDisplay - 1));
        }
        // If there is more pages, display NEXT PAGE button
        if (pageToDisplay + 1 <= pages) {
            panel.setItem(26, StaticItems.NEXT, (event) -> generateView(pageToDisplay + 1));
        }
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, region)));
    }
}

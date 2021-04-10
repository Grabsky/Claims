package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.InventoryH;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class SectionMembersAdd extends Section {
    private Player[] players;

    public SectionMembersAdd(Panel panel, Player player, UUID owner, PSRegion region) {
        super(panel, player, owner, region);
    }

    @Override
    public void prepare() {
        final int amount = Bukkit.getOnlinePlayers().size();
        this.players = Bukkit.getOnlinePlayers().toArray(new Player[amount]);
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7003");
        // Display first page of online players
        this.generateView(1, 21, players);
    }

    // TO-DO: Players are shown two times - FIX THIS
    private void generateView(int pageToDisplay, int maxOnPage, Player[] players) {
        panel.clear();
        // Updating list of members
        final ArrayList<UUID> members = region.getMembers();
        // Calculating number of pages and range
        final int size = players.length;
        final int pages = (size - 1) / maxOnPage + 1;
        // For each "use-able" slot
        for (int slot = 9, index = 0 ; index < maxOnPage; slot++) {
            if ((slot + 1) % 9 == 0 || slot % 9 == 0) continue;
            // Getting 'fixed' index
		    int fixedIndex = ((pageToDisplay * maxOnPage) - maxOnPage) + index;
            if (fixedIndex >= size) break;
            // Filtering players; Obviously not the best way to do that but at least it works ¯\_(ツ)_/¯
            if (players[fixedIndex].getUniqueId() == owner || members.contains(players[fixedIndex].getUniqueId())) {
                slot--;
                index++;
                continue;
            }
		    Player guiPlayer = players[fixedIndex];
            UUID uuid = guiPlayer.getUniqueId();
            // Add skull to gui
            panel.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a§l" + guiPlayer.getName())
                    .setLore("§7Kliknij, aby §adodać§7 do terenu.")
                    .setSkullOwner(uuid)
                    .build(), (event) -> {
                // One more check just in case something changed while GUI was open
                if (region.getMembers().size() < 10) {
                    region.addMember(uuid);
                    panel.applySection(new SectionMembers(panel, executor, owner, region));
                } else {
                    executor.closeInventory();
                    executor.sendMessage("§6§lS§e§lD§8 » §cOsiągnąłeś limit (10) graczy dodanych do terenu.");
                }

            });
            index++;
        }
        // If player is not on the first page, display PREVIOUS PAGE button
        if (pageToDisplay > 1) {
            panel.setItem(18, StaticItems.PREVIOUS, (event) -> generateView(pageToDisplay - 1, maxOnPage, players));
        }
        // If there is more pages, display NEXT PAGE button
        if (pageToDisplay + 1 <= pages) {
            panel.setItem(26, StaticItems.NEXT, (event) -> generateView(pageToDisplay + 1, maxOnPage, players));
        }
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionMembers(panel, executor, owner, region)));
    }
}

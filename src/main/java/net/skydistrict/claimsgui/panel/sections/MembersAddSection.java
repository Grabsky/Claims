package net.skydistrict.claimsgui.panel.sections;

import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class MembersAddSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private Player[] players;

    public MembersAddSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.prepare();
    }

    @Override
    public void prepare() {
        final int amount = Bukkit.getOnlinePlayers().size();
        this.players = Bukkit.getOnlinePlayers().toArray(new Player[amount]);
    }

    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7003", Containers.GENERIC_9X6);
        // Display first page of online players
        this.generatePage(1, 21, players);
    }

    // TO-DO: Players are shown two times - FIX THIS
    private void generatePage(int pageToDisplay, int maxOnPage, Player[] players) {
        panel.clear();
        // Updating list of members
        final ArrayList<UUID> members = region.getMembers();
        // Calculating number of pages and range
        final int size = players.length;
        final int pages = (size - 1) / maxOnPage + 1;
        // For each "use-able" slot
        for (int slot = 9, index = 0 ; index < maxOnPage; slot++) {
            if ((slot + 1) % 9 == 0 || slot % 9 == 0) continue;
            // Get fixed index
		    int fixedIndex = ((pageToDisplay * maxOnPage) - maxOnPage) + index;
            if (fixedIndex >= size) break;
            // Filtering players; Obviously not the best way to do that but at least it works ¯\_(ツ)_/¯
            if (players[fixedIndex] == player || members.contains(players[fixedIndex].getUniqueId())) {
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
                    .build(), event -> {
                // One more check just in case something changed while GUI was open
                if (region.getMembers().size() < 8) {
                    region.addMember(uuid);
                    panel.applySection(new MembersSection(panel, player, region));
                } else {
                    player.closeInventory();
                    player.sendMessage("§6§lS§e§lD§8 » §cOsiągnąłeś limit (8) graczy dodanych do terenu.");
                }

            });
            index++;
        }
        // If player is not on the first page, display PREVIOUS PAGE button
        if (pageToDisplay > 1) {
            panel.setItem(18, StaticItems.PREVIOUS, (event) -> generatePage(pageToDisplay - 1, maxOnPage, players));
        }
        // If there is more pages, display NEXT PAGE button
        if (pageToDisplay + 1 <= pages) {
            panel.setItem(26, StaticItems.NEXT, (event) -> generatePage(pageToDisplay + 1, maxOnPage, players));
        }
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new MembersSection(panel, player, region)));
    }
}

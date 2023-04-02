package cloud.grabsky.claims.panel.views;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public final class ViewMembersAdd implements Consumer<Panel> {

    private List<ClaimPlayer> onlineClaimPlayers = new ArrayList<>();

    private static final Component INVENTORY_TITLE = text("\u7000\u7103", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    @Override
    public void accept(final Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // ...
        final ClaimManager claimManager = cPanel.getClaim().getManager();
        final Claim claim = cPanel.getClaim();
        // ...
        this.onlineClaimPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player -> cPanel.getViewer().canSee(player) == true)
                .map(claimManager::getClaimPlayer)
                .filter(claimPlayer -> claimPlayer.isOwnerOf(claim) == false) // excluding owner(s)
                .filter(claimPlayer -> claimPlayer.isMemberOf(claim) == false) // excluding member(s)
                .toList();
        // Changing panel texture
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // Display first page of online players
        this.generate(cPanel, 1, UI_SLOTS.size());
    }

    private void generate(final ClaimPanel cPanel, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final var onlineClaimPlayersIterator = moveIteratorBefore(onlineClaimPlayers.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        final var uiSlotsIterator = UI_SLOTS.iterator();
        // ...
        final Player viewer = cPanel.getViewer();
        final Claim claim = cPanel.getClaim();
        // ...
        while (onlineClaimPlayersIterator.hasNext() == true && uiSlotsIterator.hasNext() == true) {
            final ClaimPlayer claimPlayer = onlineClaimPlayersIterator.next();
            final User user = claimPlayer.toUser();
            // ...
            final ItemStack head = new ItemBuilder(PluginItems.UI_ICON_ADD_MEMBER)
                    .setName(text(user.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                    .setSkullTexture(user.getTextures())
                    .build();
            // ...
            cPanel.setItem(uiSlotsIterator.next(), head, (event) -> {
                // One more check just in case something changed while GUI was open
                if (claim.addMember(claimPlayer) == true) {
                    cPanel.applyTemplate(new ViewMembers(), true);
                } else {
                    viewer.closeInventory();
                    Message.of(PluginLocale.UI_MEMBERS_ADD_FAILURE_REACHED_LIMIT)
                            .placeholder("limit", PluginConfig.MEMBERS_LIMIT)
                            .send(viewer);
                }
            });
        }
        // If player is not on the first page - displaying previous page button
        if (pageToDisplay > 1)
            cPanel.setItem(18, PluginItems.UI_NAVIGATION_PREVIOUS, (event) -> generate(cPanel, pageToDisplay - 1, maxOnPage));
        // If there is more players to be displayed, showing next page button
        if (onlineClaimPlayersIterator.hasNext() == true)
            cPanel.setItem(26, PluginItems.UI_NAVIGATION_NEXT, (event) -> generate(cPanel, pageToDisplay + 1, maxOnPage));
        // ...
        cPanel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> cPanel.applyTemplate(new ViewMembers(), true));
    }

    private static <T> ListIterator<T> moveIteratorBefore(final ListIterator<T> iterator, final int index) {
        while (iterator.nextIndex() != index) {
            if (iterator.nextIndex() < index)
                iterator.next();
            else if (iterator.nextIndex() > index)
                iterator.previous();
        }
        return iterator;
    }

}

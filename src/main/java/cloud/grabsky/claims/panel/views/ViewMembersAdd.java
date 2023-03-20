package cloud.grabsky.claims.panel.views;

import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
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

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static net.kyori.adventure.text.Component.text;

public class ViewMembersAdd extends ClaimPanel.View {

    private List<ClaimPlayer> onlineClaimPlayers = new ArrayList<>();

    private static final Component INVENTORY_TITLE = text("\u7000\u7103", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    @Override
    public void accept(final ClaimPanel panel) {
        final ClaimManager claimManager = panel.getClaim().getManager();
        final Claim claim = panel.getClaim();
        // ...
        this.onlineClaimPlayers = Bukkit.getOnlinePlayers().stream()
                .map(claimManager::getClaimPlayer)
                .filter(claimPlayer -> claimPlayer.isOwnerOf(claim) == false) // excluding owner(s)
                .filter(claimPlayer -> claimPlayer.isMemberOf(claim) == false) // excluding member(s)
                .toList();
        // Changing panel texture
        panel.updateClientTitle(INVENTORY_TITLE);
        // Display first page of online players
        this.generateView(panel, 1, UI_SLOTS.size());
    }

    private void generateView(final ClaimPanel panel, final int pageToDisplay, final int maxOnPage) {
        panel.clear();
        // ...
        final var onlineClaimPlayersIterator = moveIteratorBefore(onlineClaimPlayers.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        final var uiSlotsIterator = UI_SLOTS.iterator();
        // ...
        final Player viewer = panel.getViewer();
        final Claim claim = panel.getClaim();
        // ...
        while (onlineClaimPlayersIterator.hasNext() == true && uiSlotsIterator.hasNext() == true) {
            final ClaimPlayer claimPlayer = onlineClaimPlayersIterator.next();
            final User user = claimPlayer.toUser();
            // ...
            final ItemStack head = new ItemBuilder(PluginItems.ICON_ADD_MEMBER)
                    .setName(text(user.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                    .setSkullTexture(user.getTextures())
                    .build();
            // ...
            panel.setItem(uiSlotsIterator.next(), head, (event) -> {
                // One more check just in case something changed while GUI was open
                if (claim.addMember(claimPlayer) == true) {
                    panel.applyView(new ViewMembers(), true);
                } else {
                    viewer.closeInventory();
                    sendMessage(viewer, PluginLocale.REACHED_MEMBERS_LIMIT.replace("{limit}", String.valueOf(PluginConfig.MEMBERS_LIMIT)));
                }
            });
        }
        // If player is not on the first page - displaying previous page button
        if (pageToDisplay > 1)
            panel.setItem(18, PluginItems.NAVIGATION_PREVIOUS, (event) -> generateView(panel, pageToDisplay - 1, maxOnPage));
        // If there is more players to be displayed, showing next page button
        if (onlineClaimPlayersIterator.hasNext() == true)
            panel.setItem(26, PluginItems.NAVIGATION_NEXT, (event) -> generateView(panel, pageToDisplay + 1, maxOnPage));
        // ...
        panel.setItem(49, PluginItems.NAVIGATION_RETURN, (event) -> panel.applyView(new ViewMembers(), true));
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

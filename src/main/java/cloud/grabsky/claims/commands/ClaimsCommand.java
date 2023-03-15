package cloud.grabsky.claims.commands;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.ClaimLevel;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLocale;
import cloud.grabsky.claims.flags.ExtraFlags;
import cloud.grabsky.claims.panel.Panel;
import cloud.grabsky.claims.panel.sections.SectionHomes;
import cloud.grabsky.claims.panel.sections.SectionMain;
import cloud.grabsky.claims.templates.Items;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;

public class ClaimsCommand extends RootCommand {
    private final Claims claims;

    public ClaimsCommand(final Claims claims) {
        super("claims", null, "claims.command.claims", null, null);
        this.claims = claims;
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
            ? CompletionsProvider.of("edit", "fix", "get", "reload")
            : (context.getInput().at(1).equalsIgnoreCase("edit") == true)
                ? CompletionsProvider.of(Player.class)
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            this.openClaimMenu(context.getExecutor().asPlayer(), context.getExecutor().asPlayer().getUniqueId());
        } else switch (arguments.next(String.class).asOptional("help").toLowerCase()) {
            case "edit" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.edit") == true) {
                    // ...
                    final Player player = arguments.next(Player.class).asRequired(); // TO-DO: Offline player support?
                    // ...
                    this.openClaimMenu(sender, player.getUniqueId());
                    // ...
                    return;
                }
                sendMessage(sender, "No permissions.");
            }
            case "fix" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.fix") == true) {
                    final Location loc = sender.getLocation();
                    for (ProtectedRegion region : claims.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                        if (region.getId().startsWith(ClaimsConfig.REGION_PREFIX) == true) {
                            // Both variables shouldn't be null unless claim was manually modified
                            final Location center = BukkitAdapter.adapt(region.getFlag(ExtraFlags.CLAIM_CENTER));
                            final Material type = ClaimLevel.getClaimLevel(region.getFlag(ExtraFlags.CLAIM_LEVEL)).getClaimBlockMaterial();
                            PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                sendMessage(sender, ClaimsLocale.RESTORE_CLAIM_BLOCK_SUCCESS);
                            });
                            return;
                        }
                    }
                }
                sendMessage(sender, "No permissions.");
            }
            case "get" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.fix") == true) {
                    sender.getInventory().addItem(
                            ClaimLevel.COAL.getClaimBlockItem(),
                            ClaimLevel.IRON.getClaimBlockItem(),
                            ClaimLevel.GOLD.getClaimBlockItem(),
                            ClaimLevel.DIAMOND.getClaimBlockItem(),
                            ClaimLevel.EMERALD.getClaimBlockItem(),
                            ClaimLevel.NETHERITE.getClaimBlockItem(),
                            Items.UPGRADE_CRYSTAL
                    );
                    sendMessage(sender, ClaimsLocale.CLAIM_BLOCKS_ADDED);
                    return;
                }
                sendMessage(sender, "No permissions.");
            }
            case "reload" -> {
                final CommandSender sender = context.getExecutor().asCommandSender();
                if (sender.hasPermission("claims.command.claims.reload") == true) {
                    if (claims.reloadConfiguration() == true) {
                        sendMessage(sender, "success");
                        return;
                    }
                    sendMessage(sender, "failure");
                    return;
                }
                sendMessage(sender, "No permissions.");
            }
        }
    }

    private void openClaimMenu(final Player sender, final UUID ownerUniqueId) {
        final ClaimPlayer owner = claims.getClaimManager().getClaimPlayer(ownerUniqueId);
        final Panel panel = new Panel(SectionMain.INVENTORY_TITLE, owner, sender.getUniqueId().equals(owner.getUniqueId()) == false);
        if (owner.hasClaim()) {
            // This has to be run 1 tick later to update the inventory title correctly
            claims.getBedrockScheduler().run(1, (task) -> panel.applySection(new SectionMain(panel)));
            // sender.openInventory(panel);
            return;
        } else if (owner.hasRelatives()) {
            // This has to be run 1 tick later to update the inventory title correctly
            claims.getBedrockScheduler().run(1, (task) -> panel.applySection(new SectionHomes(panel)));
            // sender.openInventory(panel);
            return;
        }
        sendMessage(sender, (sender.getUniqueId().equals(ownerUniqueId)) ? ClaimsLocale.YOU_DONT_HAVE_A_CLAIM : ClaimsLocale.PLAYER_HAS_NO_CLAIM);
    }
}

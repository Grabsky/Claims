package cloud.grabsky.claims.commands;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.views.ViewMain;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.UUID;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static java.util.Comparator.comparingInt;

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
            // OPEN SELF
            this.openClaimMenu(context.getExecutor().asPlayer());
        } else switch (arguments.next(String.class).asOptional("help").toLowerCase()) {
            case "edit" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.edit") == true) {
                    // ...
                    final Player player = arguments.next(Player.class).asRequired();
                    // ...
                    this.openClaimMenu(sender, player.getUniqueId()); // +
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
                        if (region.getId().startsWith(PluginConfig.REGION_PREFIX) == true) {
                            System.out.println(region.getId());
                            // Both variables shouldn't be null unless claim was manually modified
                            final Claim claim = claims.getClaimManager().getClaim(region.getId());
                            final Location center = BukkitAdapter.adapt(region.getFlag(Claims.CustomFlag.CLAIM_CENTER));
                            final Material type = claim.getType().getBlock().getType();
                            PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                sendMessage(sender, PluginLocale.RESTORE_CLAIM_BLOCK_SUCCESS);
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
                    // Claim blocks...
                    claims.getClaimManager().getClaimTypes().values().stream().sorted(comparingInt(Claim.Type::getRadius)).forEach(type -> {
                        final ItemStack item = type.getBlock();
                        item.editMeta(meta -> {
                            meta.getPersistentDataContainer().set(Claims.Key.CLAIM_LEVEL, PersistentDataType.STRING, type.getUniqueId());
                        });
                        sender.getInventory().addItem(item);
                    });
                    // Upgrade crystal...
                    sender.getInventory().addItem(PluginItems.UPGRADE_CRYSTAL);
                    // ...
                    sendMessage(sender, PluginLocale.CLAIM_BLOCKS_ADDED);
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
            case "test" -> {
                context.getExecutor().asCommandSender().sendMessage("TEST2333");
            }
        }
    }

    // SELF; TO-DO: Panel#open(HumanEntity, PreOpenAction) is not working...
    private void openClaimMenu(final Player sender) {
        final ClaimPlayer claimPlayer = claims.getClaimManager().getClaimPlayer(sender.getUniqueId());
        // ...
        if (claimPlayer.hasClaim() == true) {
            final ClaimPanel panel = new ClaimPanel(claimPlayer.getClaim(), claimPlayer);
            // ...
            sender.openInventory(panel.getInventory());
            // ...
            claims.getBedrockScheduler().run(1L, (task) -> panel.applyView(new ViewMain(), false));
        } else {
            sendMessage(sender, "No claim... (you)");
        }

    }

    // TO-DO: Panel#open(HumanEntity, PreOpenAction) is not working...
    private void openClaimMenu(final Player sender, final UUID ownerUniqueId) {
        final ClaimPlayer claimEditor = claims.getClaimManager().getClaimPlayer(sender.getUniqueId());
        final ClaimPlayer claimOwner = claims.getClaimManager().getClaimPlayer(ownerUniqueId);
        // ...
        if (claimOwner.hasClaim() == true) {
            final ClaimPanel panel = new ClaimPanel(claimOwner.getClaim(), claimEditor);
            // ...
            sender.openInventory(panel.getInventory());
            // ...
            claims.getBedrockScheduler().run(1L, (task) -> panel.applyView(new ViewMain(), false));
        } else {
            sendMessage(sender, "No claim... (target)");
        }
    }
}

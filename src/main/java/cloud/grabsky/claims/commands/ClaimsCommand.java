package cloud.grabsky.claims.commands;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.views.ViewMain;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.argument.StringArgument;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import com.sk89q.worldedit.math.BlockVector3;
import io.papermc.lib.PaperLib;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cloud.grabsky.bedrock.components.SystemMessenger.sendMessage;
import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanel;
import static java.lang.String.valueOf;
import static java.util.Comparator.comparingInt;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

public class ClaimsCommand extends RootCommand {

    private final Claims claims;
    private final ClaimManager claimManager;

    public ClaimsCommand(final Claims claims) {
        super("claims", new String[]{ "teren", "claim" }, "claims.command.claims", null, null);
        this.claims = claims;
        this.claimManager = claims.getClaimManager();
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        return (index == 0)
            ? CompletionsProvider.of("border", "edit", "find", "get", "reload", "restore")
            : switch (context.getInput().at(1).toLowerCase()) {
                    case "edit" -> switch (index) {
                        case 1 -> CompletionsProvider.of(Claim.class);
                        case 2 -> CompletionsProvider.of("--force");
                        default -> CompletionsProvider.EMPTY;
                    };
                    case "find" -> CompletionsProvider.of(Player.class);
                    default -> CompletionsProvider.EMPTY;
            };
    }

    @SneakyThrows
    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
            // ...
            final Location location = sender.getLocation();
            // ...
            final @Nullable Claim claim = claimManager.getClaimAt(location);
            // ...
            if (claim != null) {
                if (claimSender.isOwnerOf(claim) == true || sender.hasPermission("claims.command.claims.edit") == true) {
                    if (this.openClaimMenu(sender, claim) == false)
                        sendMessage(sender, PluginLocale.CLAIMS_EDIT_FAILURE, parsed("command", context.getInput().toString()));
                    return;
                }
                sendMessage(sender, PluginLocale.NOT_CLAIM_OWNER);
                return;
            }
            sendMessage(sender, PluginLocale.NOT_IN_CLAIMED_AREA);
        } else switch (arguments.next(String.class).asOptional("").toLowerCase()) {
            case "find" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.find") == true) {
                    final OfflinePlayer offlinePlayer = arguments.next(OfflinePlayer.class).asRequired();
                    // ...
                    if (offlinePlayer.hasPlayedBefore() == true) {
                        // ...
                        final ClaimPlayer claimPlayer = claimManager.getClaimPlayer(offlinePlayer.getUniqueId());
                        // ...
                        final Set<Claim> ownedClaims = claimPlayer.getClaims();
                        // ...
                        if (ownedClaims.isEmpty() == false) {
                            sendMessage(sender, PluginLocale.CLAIMS_FIND_OWNER_OF, unparsed("count", valueOf(ownedClaims.size())), unparsed("player", offlinePlayer.getName()));
                            // ...
                            ownedClaims.forEach(claim -> {
                                sendMessage(sender, PluginLocale.CLAIMS_FIND_ENTRY, parsed("id", claim.getId())); // must be parsed to be replaced inside click event
                            });
                        }
                        sender.sendMessage("");
                        // ...
                        final Set<Claim> relativeClaims = claimPlayer.getRelativeClaims();
                        // ...
                        if (relativeClaims.isEmpty() == false) {
                            sendMessage(sender, PluginLocale.CLAIMS_FIND_MEMBER_OF, unparsed("count", valueOf(relativeClaims.size())), unparsed("player", offlinePlayer.getName()));
                            // ...
                            relativeClaims.forEach(claim -> {
                                sendMessage(sender, PluginLocale.CLAIMS_FIND_ENTRY, parsed("id", claim.getId())); // must be parsed to be replaced inside click event
                            });
                        }
                        // ...
                        if (ownedClaims.isEmpty() == true && relativeClaims.isEmpty() == true)
                            sendMessage(sender, "This player does not own nor is added any claim.");
                        // ...
                        return;
                    }
                    sendMessage(sender, "This player has never played before.");
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
            case "edit" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.edit") == true) {
                    // ...
                    final Claim claim = arguments.next(Claim.class).asRequired();
                    final String[] flags = arguments.next(String.class, StringArgument.GREEDY).asOptional("").split(" ");
                    // ...
                    if (this.openClaimMenu(sender, claim) == false) {
                        if (containsIgnoreCase(flags, "--force") == true) {
                            claim.getOwners().stream()
                                    .map(ClaimPlayer::toPlayer).filter(Objects::nonNull).filter(Player::isOnline)
                                    .map(Player::getOpenInventory).filter(ClaimPanel::isClaimPanel)
                                    .forEach(InventoryView::close);
                            // Trying one more time...
                            this.openClaimMenu(sender, claim);
                            return;
                        }
                    }
                    sendMessage(sender, PluginLocale.CLAIMS_EDIT_FAILURE, parsed("command", context.getInput().toString()));
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
            case "restore" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.restore") == true) {
                    final Location location = sender.getLocation();
                    // ...
                    final @Nullable Claim claim = claimManager.getClaimAt(location);
                    // ...
                    if (claim != null) {
                        try {
                            final Location center = claim.getCenter();
                            final Material type = claim.getType().getBlock().getType();
                            PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                sendMessage(sender, PluginLocale.CLAIMS_RESTORE_SUCCESS);
                            });
                        } catch (final ClaimProcessException e) {
                            sendMessage(sender, e.getErrorMessage());
                        }
                        return;
                    }
                    sendMessage(sender, PluginLocale.CLAIM_DOES_NOT_EXIST); // BUG: It's displayed after the forEach loop no matter what.
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
            case "get" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.get") == true) {
                    if (claimManager.getClaimTypes().isEmpty() == false) {
                        // Claim blocks...
                        claimManager.getClaimTypes().values().stream().sorted(comparingInt(Claim.Type::getRadius)).forEach(type -> {
                            final ItemStack item = type.getBlock();
                            item.editMeta(meta -> {
                                meta.getPersistentDataContainer().set(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING, type.getUniqueId());
                            });
                            sender.getInventory().addItem(item);
                        });
                        // ...
                        sendMessage(sender, PluginLocale.CLAIMS_GET_SUCCESS);
                        return;
                    }
                    sendMessage(sender, PluginLocale.CLAIMS_GET_FAILURE);
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
            case "border" -> {
                final Player sender = context.getExecutor().asPlayer();
                // ...
                if (sender.hasPermission("claims.command.claims.border") == true) {
                    // ...
                    final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
                    // ...
                    final Location location = sender.getLocation();
                    // ...
                    // Both variables shouldn't be null unless claim was manually modified
                    final @Nullable Claim claim = claimManager.getClaimAt(location);
                    // ...
                    if (claim != null) {
                        if (claimSender.isOwnerOf(claim) == true || sender.hasPermission("claims.command.claims.edit") == true) {
                            // ...
                            final Location center = claim.getCenter();
                            // ...
                            final float radius = claim.getType().getRadius() + 0.5F;
                            final float entityPosition = (claim.getType().getRadius() / 2.0F);
                            // ...
                            final BlockData data = Material.LIME_STAINED_GLASS.createBlockData();
                            final Vector3f scale = new Vector3f(radius, radius, 0);
                            // ...
                            final Iterator<Location> locations = List.of(
                                    center.clone().add(0, -0.5F, -entityPosition),
                                    center.clone().add(entityPosition, -0.5F, 0),
                                    center.clone().add(0, -0.5F, entityPosition),
                                    center.clone().add(-entityPosition, -0.5F, 0)
                            ).iterator();
                            // ...
                            final Iterator<Transformation> transformations = List.of(
                                    new Transformation(new Vector3f(-radius, 0, -entityPosition - 0.5F), new Quaternionf(0, 0, 0, 1), scale, new Quaternionf(0, 1, 0, 1)),
                                    new Transformation(new Vector3f(entityPosition + 0.5F, 0, -radius), new Quaternionf(0, -1, 0, 1), scale, new Quaternionf(0, 0, 0, 1)),
                                    new Transformation(new Vector3f(radius, 0, entityPosition + 0.5F), new Quaternionf(0, 0, 0, 1), scale, new Quaternionf(0, -1, 0, 1)),
                                    new Transformation(new Vector3f(-entityPosition - 0.5F, 0, radius), new Quaternionf(0, 1, 0, 1), scale, new Quaternionf(0, 0, 0, 1))
                            ).iterator();
                            // ...
                            while (locations.hasNext() == true && transformations.hasNext() == true) {
                                // ...
                                center.getWorld().spawnEntity(locations.next(), EntityType.BLOCK_DISPLAY, SpawnReason.CUSTOM, (entity) -> {
                                    if (entity instanceof BlockDisplay blockDisplay) {
                                        blockDisplay.setBlock(data);
                                        blockDisplay.setTransformation(transformations.next());
                                        blockDisplay.setViewRange(Float.MAX_VALUE);
                                        blockDisplay.setBrightness(new Display.Brightness(15, 15));
                                        blockDisplay.setShadowRadius(0.0F);
                                        blockDisplay.setShadowStrength(0.0F);
                                    }
                                    // ...
                                    claims.getBedrockScheduler().run(300L, (task) -> entity.remove());
                                });
                                // ...
                            }
                            return;
                        }
                        sendMessage(sender, PluginLocale.NOT_CLAIM_OWNER);
                        return;
                    }
                    sendMessage(sender, PluginLocale.NOT_IN_CLAIMED_AREA);
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
            case "reload" -> {
                final CommandSender sender = context.getExecutor().asCommandSender();
                // ...
                if (sender.hasPermission("claims.command.claims.reload") == true) {
                    if (claims.reloadConfiguration() == true) {
                        sendMessage(sender, PluginLocale.RELOAD_SUCCESS);
                        return;
                    }
                    sendMessage(sender, PluginLocale.RELOAD_FAILURE);
                    return;
                }
                sendMessage(sender, PluginLocale.MISSING_PERMISSIONS);
            }
        }
    }

    private boolean openClaimMenu(final Player sender, final Claim claim) {
        if (claim.getOwners().stream().map(ClaimPlayer::toPlayer).filter(Objects::nonNull).filter(Player::isOnline).map(Player::getOpenInventory).anyMatch(ClaimPanel::isClaimPanel) == true)
            return false;
        // ...
        new ClaimPanel(claimManager, claim).open(sender, (panel) -> {
            claims.getBedrockScheduler().run(1L, (task) -> panel.applyTemplate(new ViewMain(), false));
            return true;
        });
        // ...
        return true;
    }

    private static boolean containsIgnoreCase(final String[] arr, final String search) {
        for (final String element : arr) {
            if (search.equalsIgnoreCase(element) == true)
                return true;
        }
        return false;
    }

}

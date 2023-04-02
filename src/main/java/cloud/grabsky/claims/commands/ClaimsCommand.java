package cloud.grabsky.claims.commands;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.views.ViewMain;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanelOpen;
import static java.util.Comparator.comparingInt;
import static net.kyori.adventure.text.Component.text;

public class ClaimsCommand extends RootCommand {

    private final Claims claims;
    private final ClaimManager claimManager;

    public ClaimsCommand(final Claims claims) {
        super("claims", new String[]{"claim"}, "claims.command.claims", null, null);
        this.claims = claims;
        this.claimManager = claims.getClaimManager();
    }

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (index == 0)
            return CompletionsProvider.of(Stream.of("border", "edit", "find", "get", "reload", "restore").filter(literal -> sender.hasPermission("claims.command.claims." + literal)).toList());
        // ...
        final String literal = context.getInput().at(1).toLowerCase();
        if (sender.hasPermission("claims.command.claims." + literal) == false)
            return CompletionsProvider.EMPTY;
        // ...
        return switch (literal) {
            case "edit" -> (index == 1) ? CompletionsProvider.of(Claim.class) : CompletionsProvider.EMPTY;
            case "find" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
            default -> CompletionsProvider.EMPTY;
        };
    }

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
                if (sender.hasPermission("claims.command.claims.edit") == true || claimSender.isOwnerOf(claim) == true) {
                    if (isClaimPanelOpen(claim) == false) {
                        new ClaimPanel(claimManager, claim).open(sender, (panel) -> {
                            claims.getBedrockScheduler().run(1L, (task) -> panel.applyTemplate(new ViewMain(), false));
                            return true;
                        });
                        return;
                    }
                    Message.of(PluginLocale.CLAIMS_EDIT_FAILURE).send(sender);
                    return;
                }
                Message.of(PluginLocale.NOT_CLAIM_OWNER).send(sender);
                return;
            }
            Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
        } else switch (arguments.next(String.class).asOptional(null).toLowerCase()) {
            case "border" -> this.onClaimsBorder(context, arguments);
            case "edit" -> this.onClaimsEdit(context, arguments);
            case "find" -> this.onClaimsFind(context, arguments);
            case "get" -> this.onClaimsGet(context, arguments);
            case "reload" -> this.onClaimsReload(context, arguments);
            case "restore" -> this.onClaimsRestore(context, arguments);
        }
    }

    private void onClaimsEdit(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission("claims.command.claims.edit") == true) {
            // ...
            final Claim claim = arguments.next(Claim.class).asRequired();
            // ...
            if (isClaimPanelOpen(claim) == false) {
                new ClaimPanel(claimManager, claim).open(sender, (panel) -> {
                    claims.getBedrockScheduler().run(1L, (task) -> panel.applyTemplate(new ViewMain(), false));
                    return true;
                });
                return;
            }
            Message.of(PluginLocale.CLAIMS_EDIT_FAILURE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    @Experimental
    private void onClaimsFind(final RootCommandContext context, final ArgumentQueue arguments) {
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
                    Message.of(PluginLocale.CLAIMS_FIND_OWNER_OF)
                            .placeholder("count", ownedClaims.size())
                            .placeholder("player", offlinePlayer.getName())
                            .send(sender);
                    // ...
                    ownedClaims.forEach(claim -> {
                        Message.of(PluginLocale.CLAIMS_FIND_ENTRY)
                                .placeholder("id", text(claim.getId())) // Must be parsed to be replaced inside click event.
                                .send(sender);
                    });
                }
                // ...
                final Set<Claim> relativeClaims = claimPlayer.getRelativeClaims();
                // ...
                if (relativeClaims.isEmpty() == false) {
                    if (ownedClaims.isEmpty() == false)
                        sender.sendMessage("");
                    // ...
                    Message.of(PluginLocale.CLAIMS_FIND_MEMBER_OF)
                            .placeholder("count", relativeClaims.size())
                            .placeholder("player", offlinePlayer.getName())
                            .send(sender);
                    // ...
                    relativeClaims.forEach(claim -> {
                        Message.of(PluginLocale.CLAIMS_FIND_ENTRY)
                                .placeholder("id", text(claim.getId())) // Must be parsed to be replaced inside click event.
                                .send(sender);
                    });
                }
                // ...
                if (ownedClaims.isEmpty() == true && relativeClaims.isEmpty() == true)
                    Message.of("This player does not own nor is added any claim.").send(sender);
                // ...
                return;
            }
            Message.of("This player has never played before.").send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onClaimsGet(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission("claims.command.claims.get") == true) {
            if (claimManager.getClaimTypes().isEmpty() == false) {
                // Claim blocks...
                claimManager.getClaimTypes().values().stream().sorted(comparingInt(Claim.Type::getRadius)).forEach(type -> {
                    final ItemStack item = type.getBlock();
                    item.editMeta(meta -> {
                        meta.getPersistentDataContainer().set(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING, type.getId());
                    });
                    sender.getInventory().addItem(item);
                });
                // ...
                Message.of(PluginLocale.CLAIMS_GET_SUCCESS).send(sender);
                return;
            }
            Message.of(PluginLocale.CLAIMS_GET_FAILURE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onClaimsReload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission("claims.command.claims.reload") == true) {
            if (claims.reloadConfiguration() == true) {
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    private void onClaimsRestore(final RootCommandContext context, final ArgumentQueue arguments) {
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
                    center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> {
                        chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                        Message.of(PluginLocale.CLAIMS_RESTORE_SUCCESS).send(sender);
                    });
                } catch (final ClaimProcessException e) {
                    Message.of(e.getErrorMessage()).send(sender);
                }
                return;
            }
            Message.of(PluginLocale.CLAIM_DOES_NOT_EXIST).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    @Experimental
    private void onClaimsBorder(final RootCommandContext context, final ArgumentQueue arguments) {
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
                if (sender.hasPermission("claims.command.claims.edit") == true || claimSender.isOwnerOf(claim) == true) {
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
                            center.clone().add(0, -entityPosition - 0.5F, -entityPosition),
                            center.clone().add(entityPosition, -entityPosition - 0.5F, 0),
                            center.clone().add(0, -entityPosition - 0.5F, entityPosition),
                            center.clone().add(-entityPosition, -entityPosition - 0.5F, 0)
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
                                blockDisplay.setBrightness(new Display.Brightness(15, 0));
                                blockDisplay.setVisibleByDefault(false);
                                blockDisplay.setPersistent(false);
                                // ...
                                sender.showEntity(claims, entity);
                            }
                            claims.getBedrockScheduler().run(300L, (task) -> entity.remove());
                        });
                    }
                    return;
                }
                Message.of(PluginLocale.NOT_CLAIM_OWNER).send(sender);
                return;
            }
            Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

}

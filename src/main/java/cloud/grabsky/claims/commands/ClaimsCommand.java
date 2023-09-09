package cloud.grabsky.claims.commands;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseCategories;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
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

@Command(name = "claims", aliases = {"claim"}, permission = "claims.command.claims", usage = "/claims (...)")
public class ClaimsCommand extends RootCommand {

    // TO-DO: Replace with @Dependency.
    private final Claims plugin = Claims.getInstance();
    private final ClaimManager claimManager = Claims.getInstance().getClaimManager();

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (index == 0)
            return CompletionsProvider.of(Stream.of("border", "edit", "find", "get", "reload", "restore").filter(it -> sender.hasPermission(this.getPermission() + "." + it)).toList());
        // ...
        final String literal = context.getInput().at(1).toLowerCase();
        if (sender.hasPermission(this.getPermission() + "." + literal) == false)
            return CompletionsProvider.EMPTY;
        // ...
        return switch (literal) {
            case "edit" -> switch (index) {
                case 1 -> CompletionsProvider.of(Claim.class);
                case 2 -> CompletionsProvider.of("--force");
                default -> CompletionsProvider.EMPTY;
            };
            case "find" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
            case "restore" -> (index == 1) ? CompletionsProvider.of(Claim.class) : CompletionsProvider.EMPTY;
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
                if (sender.hasPermission(this.getPermission() + ".edit") == true || claimSender.isOwnerOf(claim) == true) {
                    if (isClaimPanelOpen(claim) == false) {
                        new ClaimPanel.Builder()
                                .setClaimManager(claimManager)
                                .setClaim(claim)
                                .build()
                                .open(sender, (panel) -> {
                                    plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseCategories.INSTANCE, false));
                                    return true;
                                });
                        return;
                    }
                    Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_FAILURE_ALREADY_IN_USE).send(sender);
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

    /* CLAIMS EDIT */

    private static final ExceptionHandler.Factory CLAIMS_EDIT_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onClaimsEdit(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".edit") == true) {
            // ...
            final Claim claim = arguments.next(Claim.class).asRequired(CLAIMS_EDIT_USAGE);
            // ...
            if (isClaimPanelOpen(claim) == false) {
                new ClaimPanel.Builder()
                        .setClaimManager(claimManager)
                        .setClaim(claim)
                        .build()
                        .open(sender, (panel) -> {
                            plugin.getBedrockScheduler().run(1L, (task) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseCategories.INSTANCE, false));
                            return true;
                        });
                return;
            }
            Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_FAILURE_ALREADY_IN_USE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS FIND */

    private static final ExceptionHandler.Factory CLAIMS_FIND_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_FIND_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    @Experimental // TO-DO: Fix UUID arguments reporting invalid players for new players (may have something to do with #hasPlayedBefore check)
    private void onClaimsFind(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".find") == true) {
            final OfflinePlayer target = arguments.next(OfflinePlayer.class).asRequired(CLAIMS_FIND_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".find.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting ClaimPlayer instance of specified player.
            final ClaimPlayer cTarget = claimManager.getClaimPlayer(target.getUniqueId());
            // Getting list of claims owned by specified player.
            final Set<Claim> ownedClaims = cTarget.getClaims();
            // Sending specialized message in case target is not owner of any claim.
            if (ownedClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_OWNER_OF_NONE).placeholder("player", target.getName()).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_OWNER_OF_HEADER)
                        .placeholder("player", target.getName())
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                ownedClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_FIND_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_OWNER_OF_FOOTER)
                        .placeholder("player", target.getName())
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
            }
            // Getting list of claims specified player is member of.
            final Set<Claim> relativeClaims = cTarget.getRelativeClaims();
            // Sending specialized message in case target is not member of any claim.
            if (relativeClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_MEMBER_OF_NONE).placeholder("player", target.getName()).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_MEMBER_OF_HEADER)
                        .placeholder("player", target.getName())
                        .placeholder("count", relativeClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                relativeClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_FIND_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_FIND_MEMBER_OF_FOOTER)
                        .placeholder("player", target.getName())
                        .placeholder("count", relativeClaims.size())
                        .send(sender);
            }
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS GET */

    private void onClaimsGet(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".get") == true) {
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
                Message.of(PluginLocale.COMMAND_CLAIMS_GET_SUCCESS).send(sender);
                return;
            }
            Message.of(PluginLocale.COMMAND_CLAIMS_GET_FAILURE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS RELOAD */

    private void onClaimsReload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".reload") == true) {
            if (plugin.reloadConfiguration() == true) {
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS RESTORE */

    private static final ExceptionHandler.Factory CLAIMS_RESTORE_USAGE = (exception) -> {
        if (exception instanceof MissingInputException)
            return (ExceptionHandler<CommandLogicException>) (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_RESTORE_USAGE).send(context.getExecutor().asCommandSender());
        // Let other exceptions be handled internally.
        return null;
    };

    private void onClaimsRestore(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".restore") == true) {
            final Claim claim = arguments.next(Claim.class).asRequired(CLAIMS_RESTORE_USAGE);
            // ...
            try {
                final Location center = claim.getCenter();
                final Material type = claim.getType().getBlock().getType();
                center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> {
                    chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                    Message.of(PluginLocale.COMMAND_CLAIMS_RESTORE_SUCCESS).send(sender);
                });
            } catch (final ClaimProcessException e) {
                Message.of(e.getErrorMessage()).send(sender);
            }
            return;
        }
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS BORDER */

    @Experimental
    private void onClaimsBorder(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".border") == true) {
            // ...
            final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
            // ...
            final Location location = sender.getLocation();
            // ...
            // Both variables shouldn't be null unless claim was manually modified
            final @Nullable Claim claim = claimManager.getClaimAt(location);
            // ...
            if (claim != null) {
                if (sender.hasPermission(this.getPermission() + ".edit") == true || claimSender.isOwnerOf(claim) == true) {
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
                                sender.showEntity(plugin, entity);
                            }
                            plugin.getBedrockScheduler().run(300L, (task) -> entity.remove());
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

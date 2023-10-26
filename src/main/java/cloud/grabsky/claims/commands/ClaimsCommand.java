package cloud.grabsky.claims.commands;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
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
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.component.ExceptionHandler;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import io.papermc.paper.math.BlockPosition;
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
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static cloud.grabsky.claims.panel.ClaimPanel.isClaimPanelOpen;
import static cloud.grabsky.claims.util.Utilities.toChunkPosition;
import static java.util.Comparator.comparingInt;

@Command(name = "claims", aliases = {"claim"}, permission = "claims.command.claims", usage = "/claims (...)")
public class ClaimsCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Claims plugin = Claims.getInstance();

    @Dependency
    private @UnknownNullability ClaimManager claimManager = Claims.getInstance().getClaimManager();

    @Override
    public @NotNull CompletionsProvider onTabComplete(final @NotNull RootCommandContext context, final int index) throws CommandLogicException {
        // Getting the first argument (second input element) from command input.
        final String argument = context.getInput().at(1, "").toLowerCase();
        // Displaying list of sub-commands in case no argument has been provided.
        if (argument.isEmpty() == true)
            return CompletionsProvider.filtered(it -> context.getExecutor().hasPermission(this.getPermission() + "." + it), "border", "edit", "list", "get", "reload", "restore");
        // Otherwise, checking permissions and sending specialized permissions to the sender.
        return (context.getExecutor().hasPermission(this.getPermission() + "." + argument) == true)
                ? switch (argument) {
                    case "edit" -> switch (index) {
                        case 1 -> CompletionsProvider.of(Claim.class);
                        case 2 -> CompletionsProvider.of("--force");
                        // Displaying no completions for higher indexes.
                        default -> CompletionsProvider.EMPTY;
                    };
                    case "list" -> (index == 1) ? CompletionsProvider.of(Player.class) : CompletionsProvider.EMPTY;
                    case "restore" -> (index == 1) ? CompletionsProvider.of(Claim.class) : CompletionsProvider.EMPTY;
                    // Displaying no completions in case unrecognized argument has been provided.
                    default -> CompletionsProvider.EMPTY;
                }
                // Displaying no completions in case command executor is not authorized to use that sub-command.
                : CompletionsProvider.EMPTY;
    }

    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        // Displaying help in case no arguments has been provided.
        if (arguments.hasNext() == false) {
            final Player sender = context.getExecutor().asPlayer();
            final ClaimPlayer claimSender = claimManager.getClaimPlayer(sender);
            // Getting location of the command sender.
            final Location location = sender.getLocation();
            // Getting Claim at location of the command sender.
            final @Nullable Claim claim = claimManager.getClaimAt(location);
            // ...
            if (claim != null) {
                // Checking whether sender is owner of that claim or has ability to edit claims of other players.
                if (claimSender.isOwnerOf(claim) == true || sender.hasPermission(this.getPermission() + ".edit") == true) {
                    // Checking whether the claim panel is already open.
                    if (isClaimPanelOpen(claim) == false) {
                        // Building new instance of ClaimPanel and opening it to the command sender.
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
                    // Sending error message to command sender.
                    Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_FAILURE_ALREADY_IN_USE).send(sender);
                    return;
                }
                // Sending error message to command sender.
                Message.of(PluginLocale.NOT_CLAIM_OWNER).send(sender);
                return;
            }
            // Sending error message to command sender.
            Message.of(PluginLocale.NOT_IN_CLAIMED_AREA).send(sender);
        // Otherwise, executing specialized sub-command logic.
        } else switch (arguments.next(String.class).asRequired().toLowerCase()) {
            case "border" -> this.onClaimsBorder(context, arguments);
            case "edit" -> this.onClaimsEdit(context, arguments);
            case "get" -> this.onClaimsGet(context, arguments);
            case "list" -> this.onClaimsList(context, arguments);
            case "reload" -> this.onClaimsReload(context, arguments);
            case "restore" -> this.onClaimsRestore(context, arguments);
            // Displaying help in case unrecognized argument has been provided.
            default -> this.onHelp(context);
        }
    }

    /* CLAIMS */

    private void onHelp(final @NotNull RootCommandContext context) {
        Message.of(PluginLocale.COMMAND_CLAIMS_USAGE).send(context.getExecutor());
    }

    /* CLAIMS EDIT */

    private static final ExceptionHandler.Factory CLAIMS_EDIT_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_EDIT_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
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

    /* CLAIMS LIST */

    private static final ExceptionHandler.Factory CLAIMS_LIST_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_LIST_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    @Experimental // TO-DO: Fix UUID arguments reporting invalid players for new players (may have something to do with #hasPlayedBefore check)
    private void onClaimsList(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".list") == true) {
            final OfflinePlayer target = (sender instanceof Player senderPlayer)
                    ? arguments.next(OfflinePlayer.class).asOptional(senderPlayer)
                    : arguments.next(OfflinePlayer.class).asRequired(CLAIMS_LIST_USAGE);
            // In case specified target is not sender, checking permissions.
            if (sender.equals(target) == false && sender.hasPermission(this.getPermission() + ".list.others") == false) {
                Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
                return;
            }
            // Getting ClaimPlayer instance of specified player.
            final ClaimPlayer cTarget = claimManager.getClaimPlayer(target.getUniqueId());
            // Getting list of claims owned by specified player.
            final Set<Claim> ownedClaims = cTarget.getClaims();
            // Sending specialized message in case target is not owner of any claim.
            if (ownedClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_NONE).placeholder("player", target).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_HEADER)
                        .placeholder("player", target)
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                ownedClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_LIST_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_OWNER_OF_FOOTER)
                        .placeholder("player", target)
                        .placeholder("count", ownedClaims.size())
                        .send(sender);
            }
            // Getting list of claims specified player is member of.
            final Set<Claim> relativeClaims = cTarget.getRelativeClaims();
            // Sending specialized message in case target is not member of any claim.
            if (relativeClaims.isEmpty() == true)
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_NONE).placeholder("player", target).send(sender);
            // Otherwise, listing whatever was found...
            else {
                // Sending output header to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_HEADER)
                        .placeholder("player", target)
                        .placeholder("count", relativeClaims.size())
                        .send(sender);
                // Iterating over claims and listing each of them to the sender.
                relativeClaims.forEach(claim -> {
                    Message.of(PluginLocale.COMMAND_CLAIMS_LIST_ENTRY)
                            .replace("<claim>", claim.getId()) // Must be a direct replacement because placeholders do not work in click events.
                            .placeholder("claim_displayname", claim.getDisplayName())
                            .placeholder("claim_location", claim.getCenter())
                            .send(sender);
                });
                // Sending output footer to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_LIST_MEMBER_OF_FOOTER)
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
                // Iterating over list of claims.
                claimManager.getClaimTypes().values().stream().sorted(comparingInt(Claim.Type::getRadius)).forEach(type -> {
                    // Getting identifier of this claim type.
                    final String id = type.getId();
                    // Getting block item of this claim type.
                    final ItemStack blockItem = type.getBlock();
                    // Adding the block item to sender's inventory.
                    sender.getInventory().addItem(
                            new ItemBuilder(blockItem).setPersistentData(Claims.Key.CLAIM_TYPE, PersistentDataType.STRING, id).build()
                    );
                });
                // Sending success message to the sender.
                Message.of(PluginLocale.COMMAND_CLAIMS_GET_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.COMMAND_CLAIMS_GET_FAILURE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS RELOAD */

    private void onClaimsReload(final RootCommandContext context, final ArgumentQueue arguments) {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // ...
        if (sender.hasPermission(this.getPermission() + ".reload") == true) {
            // Reloading the claims cache...
            claimManager.cacheClaims();
            // Reloading the plugin...
            if (plugin.reloadConfiguration() == true) {

                // Sending success message to the sender.
                Message.of(PluginLocale.RELOAD_SUCCESS).send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of(PluginLocale.RELOAD_FAILURE).send(sender);
            return;
        }
        // Sending error message to the sender.
        Message.of(PluginLocale.MISSING_PERMISSIONS).send(sender);
    }

    /* CLAIMS RESTORE */

    private static final ExceptionHandler.Factory CLAIMS_RESTORE_USAGE = (exception) -> {
        return (exception instanceof MissingInputException)
                ? (e, context) -> Message.of(PluginLocale.COMMAND_CLAIMS_RESTORE_USAGE).send(context.getExecutor())
                : null; // Let other exceptions be handled internally.
    };

    private void onClaimsRestore(final RootCommandContext context, final ArgumentQueue arguments) {
        final Player sender = context.getExecutor().asPlayer();
        // ...
        if (sender.hasPermission(this.getPermission() + ".restore") == true) {
            // Getting next argument as Claim.
            final Claim claim = arguments.next(Claim.class).asRequired(CLAIMS_RESTORE_USAGE);
            // Trying...
            try {
                // Getting center location of the claim.
                final Location center = claim.getCenter();
                // Getting type of claim block of the claim.
                final Material type = claim.getType().getBlock().getType();
                // Getting chunk at claim center asynchronously...
                center.getWorld().getChunkAtAsync(center).thenAccept(chunk -> {
                    // Getting claim center position in chunk.
                    final BlockPosition position = toChunkPosition(center);
                    // Restoring claim block at center position of this claim.
                    chunk.getBlock(position.blockX(), position.blockY(), position.blockZ()).setType(type);
                    // Sending success message to the sender.
                    Message.of(PluginLocale.COMMAND_CLAIMS_RESTORE_SUCCESS).send(sender);
                });
            } catch (final ClaimProcessException e) {
                // Sending error message to the sender.
                Message.of(e.getErrorMessage()).send(sender);
                // Logging error message to the console
                claimManager.getPlugin().getLogger().warning("An error occurred while trying to access claim:");
                claimManager.getPlugin().getLogger().warning("   " + e.getMessage());
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

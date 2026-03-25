/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.session;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.exception.ClaimProcessException;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseOwnedClaims;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.waypoints.Waypoint;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;

public enum RenameSession implements Listener {
    INSTANCE; // SINGLETON

    private static final Cache<UUID, String> CURRENTLY_OPEN_DIALOGS = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(5)).build();

    // Listening to PlayerQuitEvent to invalidate cached data when it is no longer needed.
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        CURRENTLY_OPEN_DIALOGS.invalidate(event.getPlayer().getUniqueId());
    }

    public static boolean isSessionActive(final @NotNull Player player, final @Nullable Location location) {
        final var cachedIdentifier = CURRENTLY_OPEN_DIALOGS.getIfPresent(player.getUniqueId());
        return (location != null && getAccessBlockLocationString(location).equals(cachedIdentifier) == true);
    }

    private static @NotNull String getAccessBlockLocationString(final @NotNull Location location) {
        return location.x() + ", " + location.y() + ", " + location.z();
    }

    private static @Nullable String transformInput(final @Nullable String input) {
        // Trimming whitespaces. If input is null, this will be set to a blank string instead.
        final var trimmed = (input != null) ? input.trim().replace("  ", " ") : "";
        // Returning trimmed input if it's not blank and is within the valid range.
        return (trimmed.isBlank() == false && inRange(trimmed.length(), 1, 32) == true) ? trimmed : null;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void openRenameDialog(final @NotNull Player audience, final @NotNull String initial, final @NotNull ClaimPanel associatedPanel, final @NotNull Consumer<DialogResponseView> onConfirm) {
        final var uniqueId = audience.getUniqueId();
        final var dialog = Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(
                        ActionButton.builder(PluginLocale.UI_RENAME_PANEL_BUTTON_CONFIRM)
                                .action(DialogAction.customClick((response, _) -> {
                                    onConfirm.accept(response);
                                    CURRENTLY_OPEN_DIALOGS.invalidate(uniqueId);
                                }, CALLBACK_OPTIONS))
                                .build(),
                        ActionButton.builder(PluginLocale.UI_RENAME_PANEL_BUTTON_CANCEL)
                                .action(DialogAction.staticAction(ClickEvent.callback((_) -> {
                                    CURRENTLY_OPEN_DIALOGS.invalidate(uniqueId);
                                    associatedPanel.open(audience, null);
                                })))
                                .build()))
                .base(DialogBase.builder(PluginLocale.UI_RENAME_PANEL_TITLE)
                        .inputs(List.of(
                                DialogInput.text("name_input", PluginLocale.UI_RENAME_PANEL_INPUT_LABEL)
                                        .initial(initial)
                                        .maxLength(32)
                                        .build()
                        ))
                        .build())
        );
        // Closing current inventory and showing the dialog.
        audience.closeInventory();
        audience.showDialog(dialog);
        // Generating location string from access block location.
        var location = associatedPanel.getAccessBlockLocation();
        // Adding currently open dialog to the cache.
        if (location != null)
            CURRENTLY_OPEN_DIALOGS.put(uniqueId, getAccessBlockLocationString(location));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void openWaypointRenameDialog(final @NotNull Player audience, final @NotNull Waypoint waypoint, final @NotNull ClaimPanel associatedPanel) {
        openRenameDialog(audience, waypoint.getDisplayName(), associatedPanel, (response) -> {
            final var name = transformInput(response.getText("name_input"));
            // Returning for invalid input.
            if (name == null) {
                Message.of(PluginLocale.UI_WAYPOINT_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            // Renaming...
            Claims.getInstance().getWaypointManager().renameWaypoint(waypoint.getOwner(), waypoint, name).thenAccept(isSuccess -> {
                // Returning for rename failures.
                if (isSuccess == false) {
                    Message.of(PluginLocale.UI_WAYPOINT_RENAME_FAILURE_NO_LONGER_EXISTS).send(audience);
                    return;
                }
                // Sending success message to the sender.
                Message.of(PluginLocale.UI_WAYPOINT_RENAME_SUCCESS).placeholder("name", name).send(audience);
                // Re-opening the waypoints panel. Must be scheduled onto the main thread.
                Claims.getInstance().getBedrockScheduler().run(0L, (_) -> {
                    new ClaimPanel.Builder()
                            .setClaimManager(Claims.getInstance().getClaimManager())
                            .setClaim(associatedPanel.getClaim())
                            .setAccessBlockLocation(associatedPanel.getAccessBlockLocation())
                            .build().open(audience, (panel) -> {
                                Claims.getInstance().getBedrockScheduler().run(1L, (_) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
                                return true;
                            });
                });
            });
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void openClaimRenameDialog(final @NotNull Player audience, final @NotNull Claim claim, final @NotNull ClaimPanel associatedPanel) {
        openRenameDialog(audience, claim.getDisplayName(), associatedPanel, (response) -> {
            final var name = transformInput(response.getText("name_input"));
            // Returning for invalid input.
            if (name == null) {
                Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            try {
                // Renaming...
                claim.setDisplayName(name);
            } catch (final ClaimProcessException e) {
                Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_NO_LONGER_EXISTS).send(audience);
                return;
            }
            // Sending success message to the sender.
            Message.of(PluginLocale.UI_CLAIM_RENAME_SUCCESS).placeholder("name", name).send(audience);
            // Re-opening the claims panel.
            new ClaimPanel.Builder()
                    .setClaimManager(Claims.getInstance().getClaimManager())
                    .setClaim(claim)
                    .setAccessBlockLocation(associatedPanel.getAccessBlockLocation())
                    .build().open(audience, (panel) -> {
                        Claims.getInstance().getBedrockScheduler().run(1L, (_) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseOwnedClaims.INSTANCE, true));
                        return true;
                    });
        });
    }
}

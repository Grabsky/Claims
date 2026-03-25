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
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import static cloud.grabsky.bedrock.helpers.Conditions.inRange;

public class Session {

    @SuppressWarnings("UnstableApiUsage")
    private static void openRenameDialog(final @NotNull Player audience, final @NotNull String initial, final @NotNull DialogActionCallback onConfirm) {
        final var dialog = Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(
                        ActionButton.builder(PluginLocale.UI_RENAME_PANEL_BUTTON_CONFIRM)
                                .action(DialogAction.customClick(onConfirm, ClickCallback.Options.builder().lifetime(Duration.ofMinutes(5)).build()))
                                .build(),
                        ActionButton.builder(PluginLocale.UI_RENAME_PANEL_BUTTON_CANCEL).build()))
                .base(DialogBase.builder(PluginLocale.UI_RENAME_PANEL_TITLE)
                        .inputs(List.of(
                                DialogInput.text("name_input", PluginLocale.UI_RENAME_PANEL_INPUT_LABEL)
                                        .initial(initial)
                                        .maxLength(32)
                                        .build()
                        ))
                        .build())
        );
        audience.closeInventory();
        audience.showDialog(dialog);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void openWaypointRenameDialog(final @NotNull Player audience, final @NotNull Waypoint waypoint, final @NotNull ClaimPanel associatedPanel) {
        openRenameDialog(audience, waypoint.getDisplayName(), (response, _) -> {
            final var rawName = response.getText("name_input");
            // Returning if invalid input.
            if (rawName == null || rawName.isBlank() == true) {
                Message.of(PluginLocale.UI_WAYPOINT_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            // Trimming whitespaces and replacing all double-whitespaces with single whitespace.
            final var name = rawName.trim().replace("  ", " ");
            // Returning if invalid input.
            if (inRange(name.length(), 1, 32) == false) {
                Message.of(PluginLocale.UI_WAYPOINT_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            // Renaming...
            Claims.getInstance().getWaypointManager().renameWaypoint(waypoint.getOwner(), waypoint, name).thenAccept(isSuccess -> {
                // Returning for rename failures (this usually means invalid input).
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
        openRenameDialog(audience, claim.getDisplayName(), (response, _) -> {
            final var rawName = response.getText("name_input");
            // Returning for invalid input.
            if (rawName == null || rawName.isBlank() == true) {
                Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            // Trimming whitespaces and replacing all double-whitespaces with single whitespace.
            final var name = rawName.trim().replace("  ", " ");
            // Returning if invalid input.
            if (inRange(name.length(), 1, 32) == false) {
                Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_INVALID_STRING).send(audience);
                return;
            }
            // Renaming...
            try {
                final var isSuccess = claim.setDisplayName(name);
                // Returning for rename failures (this usually means invalid input).
                if (isSuccess == false) {
                    Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_INVALID_STRING).send(audience);
                    return;
                }
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

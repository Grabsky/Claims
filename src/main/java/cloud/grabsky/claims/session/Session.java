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
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.panel.templates.BrowseOwnedClaims;
import cloud.grabsky.claims.panel.templates.BrowseWaypoints;
import cloud.grabsky.claims.waypoints.Waypoint;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public abstract class Session<T> {

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Claims plugin = Claims.getInstance();

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull T subject;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull ClaimPanel associatedPanel;


    public static final class WaypointRenameSession extends Session<Waypoint> {

        public WaypointRenameSession(final @NotNull Waypoint subject, final @NotNull ClaimPanel associatedPanel) {
            super(subject, associatedPanel);
        }

    }

    public static final class ClaimRenameSession extends Session<Claim> {

        public ClaimRenameSession(final @NotNull Claim subject, final @NotNull ClaimPanel associatedPanel) {
            super(subject, associatedPanel);
        }

    }

    public enum Listener implements org.bukkit.event.Listener {
        /* SINGLETON */ INSTANCE;

        private static final int MAX_PLAYERS = Bukkit.getMaxPlayers();

        public static final Cache<UUID, Session<?>> CURRENT_EDIT_SESSIONS = CacheBuilder.newBuilder()
                .initialCapacity(MAX_PLAYERS)
                .expireAfterWrite(PluginConfig.CLAIM_SETTINGS_RENAME_PROMPT_DURATION, TimeUnit.SECONDS)
                .removalListener(notification -> {
                    if (notification.getValue() instanceof ClaimRenameSession session) {
                        session.getSubject().setPendingRename(false);
                        // Marking claim as no longer being edited upon session expire. This means that user took too long to choose a new name and interface has not been re-opened.
                        // To make sure this is not called when user decides to open panel during the rename session, any session should be removed whenever it gets opened.
                        if (notification.getCause() == RemovalCause.EXPIRED && session.getAssociatedPanel().getClaim() != null)
                            session.getAssociatedPanel().getClaim().setBeingEdited(false);
                    } else if (notification.getValue() instanceof WaypointRenameSession session) {
                        session.getSubject().setPendingRename(false);
                        // Marking claim as no longer being edited upon session expire. This means that user took too long to choose a new name and interface has not been re-opened.
                        // To make sure this is not called when user decides to open panel during the rename session, any session should be removed whenever it gets opened.
                        if (notification.getCause() == RemovalCause.EXPIRED && session.getAssociatedPanel().getClaim() != null)
                            session.getAssociatedPanel().getClaim().setBeingEdited(false);
                    }
                })
                .build();

        @EventHandler
        public void onQuit(final @NotNull PlayerQuitEvent event) {
            final UUID uniqueId = event.getPlayer().getUniqueId();
            // Invalidating session on player quit.
            if (CURRENT_EDIT_SESSIONS.asMap().containsKey(uniqueId) == true)
                CURRENT_EDIT_SESSIONS.invalidate(uniqueId);
        }

        @SuppressWarnings("UnstableApiUsage")
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChatDecorate(final @NotNull AsyncChatDecorateEvent event) {
            // Returning for non-player event calls.
            if (event.player() == null)
                return;
            // Getting the unique id of the player. This is not null.
            final UUID uniqueId = event.player().getUniqueId();
            // Cancelling the event if there is an active session.
            if (CURRENT_EDIT_SESSIONS.asMap().containsKey(uniqueId) == true && CURRENT_EDIT_SESSIONS.getIfPresent(uniqueId) != null)
                event.setCancelled(true);
        }

        // TO-DO: Merge common logic.
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChat(final @NotNull AsyncChatEvent event) {
            final Player player = event.getPlayer();
            final UUID uniqueId = event.getPlayer().getUniqueId();
            // Handling existing session.
            if (CURRENT_EDIT_SESSIONS.asMap().containsKey(uniqueId) == true) {
                final @Nullable Session<?> abstractSession = CURRENT_EDIT_SESSIONS.getIfPresent(uniqueId);
                if (abstractSession != null) {
                    // Cancelling the event; we don't want the message to be passed through
                    event.setCancelled(true);
                    // Converting Component message to a String
                    final String message = PlainTextComponentSerializer.plainText().serialize(event.message());
                    // ...
                    final @Nullable Claim associatedClaim = abstractSession.getAssociatedPanel().getClaim();
                    final @Nullable Location accessedBlock = abstractSession.getAssociatedPanel().getAccessBlockLocation();
                    // ...
                    if (abstractSession instanceof WaypointRenameSession session) {
                        final Waypoint waypoint = session.getSubject(); // Cannot be null in that context.
                        final @Nullable ClaimPanel associatedPanel = session.getAssociatedPanel();
                        // Trying to set claim display name...
                        session.getPlugin().getWaypointManager().renameWaypoint(uniqueId, waypoint, message).thenAccept(isSuccess -> {
                            if (isSuccess == true) {
                                // Invalidating the session.
                                CURRENT_EDIT_SESSIONS.invalidate(uniqueId);
                                // ...
                                waypoint.setPendingRename(false);
                                // Setting this to keep the panel "busy" while name is being changed.
                                if (associatedClaim != null) {
                                    associatedClaim.setPendingRename(true);
                                }
                                // ...
                                session.getPlugin().getBedrockScheduler().run(0L, (task) -> {
                                    // Clearing the title.
                                    player.clearTitle();
                                    // Opening (new) inventory to the player.
                                    new ClaimPanel.Builder()
                                            .setClaimManager(session.getPlugin().getClaimManager())
                                            .setClaim(associatedClaim)
                                            .setAccessBlockLocation(accessedBlock)
                                            .build().open(player, (panel) -> {
                                                session.getPlugin().getBedrockScheduler().run(1L, (___) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
                                                return true;
                                            });
                                });
                                // Sending success message.
                                Message.of(PluginLocale.UI_WAYPOINT_RENAME_SUCCESS).placeholder("name", waypoint.getDisplayName()).send(player);
                                // Returning...
                                return;
                            }
                            // Sending error message otherwise.
                            Message.of(PluginLocale.UI_WAYPOINT_RENAME_FAILURE_INVALID_STRING).send(player);
                        });
                    } else if (abstractSession instanceof ClaimRenameSession session) {
                        final Claim claim = session.getSubject(); // Cannot be null in that context.
                        // Trying to set claim display name...
                        if (claim.setDisplayName(message) == true) {
                            claim.setPendingRename(true);
                            // Invalidating the session.
                            CURRENT_EDIT_SESSIONS.invalidate(uniqueId);
                            // ...
                            session.getPlugin().getBedrockScheduler().run(0L, (task) -> {
                                // Clearing the title.
                                player.clearTitle();
                                // Opening (new) inventory to the player.
                                new ClaimPanel.Builder()
                                        .setClaimManager(claim.getManager())
                                        .setClaim(claim)
                                        .setAccessBlockLocation(accessedBlock)
                                        .build().open(player, (panel) -> {
                                            session.getPlugin().getBedrockScheduler().run(1L, (___) -> ((ClaimPanel) panel).applyClaimTemplate(BrowseOwnedClaims.INSTANCE, true));
                                            return true;
                                        });
                            });
                            // Sending success message.
                            Message.of(PluginLocale.UI_CLAIM_RENAME_SUCCESS).placeholder("name", claim.getDisplayName()).send(player);
                            // Returning...
                            return;
                        }
                        // Sending error message otherwise.
                        Message.of(PluginLocale.UI_CLAIM_RENAME_FAILURE_INVALID_STRING).send(player);
                    }
                }
            }
        }
    }

}

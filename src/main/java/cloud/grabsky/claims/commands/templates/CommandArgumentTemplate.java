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
package cloud.grabsky.claims.commands.templates;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.commands.argument.ClaimArgument;
import cloud.grabsky.commands.RootCommandManager;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CommandArgumentTemplate implements Consumer<RootCommandManager> {

    private final Claims plugin;

    @Override
    public void accept(final @NotNull RootCommandManager manager) {
        final ClaimArgument argument = new ClaimArgument(plugin.getClaimManager());
        // Claim
        manager.setArgumentParser(Claim.class, argument);
        manager.setCompletionsProvider(Claim.class, argument);
    }

}

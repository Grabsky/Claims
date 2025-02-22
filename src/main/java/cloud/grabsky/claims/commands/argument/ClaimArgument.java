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
package cloud.grabsky.claims.commands.argument;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimArgument implements CompletionsProvider, ArgumentParser<Claim> {

    private final ClaimManager claimManager;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) throws CommandLogicException {
        return Stream.concat(Stream.of("@claim"), claimManager.getClaims().stream().map(Claim::getId)).toList();
    }

    @Override
    public Claim parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        // ...
        if ("@claim".equalsIgnoreCase(value) == true && context.getExecutor().isPlayer() == true) {
            final @Nullable Claim claim = claimManager.getClaimAt(context.getExecutor().asPlayer().getLocation());
            if (claim != null)
                return claim;
        }
        // ...
        final Claim claim = claimManager.getClaim(value);
        // ...
        if (claim == null)
            throw new ClaimArgument.Exception(value);
        // ...
        return claim;
    }

    public static final class Exception extends ArgumentParseException {

        public Exception(final String inputValue) {
            super(inputValue);
        }

        public Exception(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final @NotNull RootCommandContext context) {
            final CommandSender sender = context.getExecutor().asCommandSender();
            if ("@claim".equals(this.getInputValue()) == true) {
                Message.of(PluginLocale.ARGUMENT_CLAIM_NOT_IN_CLAIMED_AREA).send(sender);
                return;
            }
            Message.of(PluginLocale.ARGUMENT_CLAIM_NOT_FOUND).placeholder("claim", this.getInputValue()).send(sender);
        }

    }

}

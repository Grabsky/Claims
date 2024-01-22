/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

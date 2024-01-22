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

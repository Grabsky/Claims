package cloud.grabsky.claims.commands.argument;

import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimArgument implements CompletionsProvider, ArgumentParser<Claim> {

    private final ClaimManager manager;

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) throws CommandLogicException {
        return manager.getClaims().stream().map(Claim::getId).toList();
    }

    @Override
    public Claim parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        // ...
        final Claim claim = manager.getClaim(value);
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

    }

}

package cloud.grabsky.claims.commands.templates;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.commands.argument.ClaimArgument;
import cloud.grabsky.commands.RootCommandManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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

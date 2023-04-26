package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.inventory.Panel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BrowseOwnedClaims implements Consumer<Panel> {

    @Override
    public void accept(final @NotNull Panel panel) {

    }

}

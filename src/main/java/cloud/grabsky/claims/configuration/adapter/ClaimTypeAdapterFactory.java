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
package cloud.grabsky.claims.configuration.adapter;

import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.configuration.util.LazyInit;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonReader.Token;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.squareup.moshi.Types.getRawType;
import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimTypeAdapterFactory implements JsonAdapter.Factory {

    @Getter(AccessLevel.PUBLIC)
    private final ClaimManager claimManager;

    @Override
    public @Nullable JsonAdapter<Claim.Type> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
        if (Claim.Type.class.isAssignableFrom(getRawType(type)) == false)
            return null;
        // ...
        final JsonAdapter<ItemStack> adapter0 = moshi.adapter(ItemStack.class);
        final JsonAdapter<ItemStack[]> adapter1 = moshi.adapter(ItemStack[].class);
        // ...
        return new JsonAdapter<>() {

            @Override
            public @NotNull Claim.Type fromJson(final @NotNull JsonReader in) throws IOException {
                in.beginObject();
                // ...
                final ClaimTypeInit init = new ClaimTypeInit();
                // ...
                while (in.hasNext() == true) {
                    final String nextName = in.nextName().toLowerCase();
                    switch (nextName) {
                        case "id" -> init.id = in.nextString();
                        case "radius" -> init.radius = in.nextInt();
                        case "block" -> init.block = adapter0.fromJson(in);
                        case "upgrade" -> {
                            in.beginObject();
                            while (in.hasNext() == true) {
                                final String nextNextName = in.nextName().toLowerCase();
                                switch (nextNextName) {
                                    case "upgrade_button" -> init.upgradeButton = adapter0.fromJson(in);
                                    case "upgrade_cost" -> init.upgradeCost = (in.peek() == Token.NULL) ? null : adapter1.fromJson(in);
                                    default -> throw new JsonDataException("Unexpected field at " + in.getPath() + ": " + nextNextName);
                                }
                            }
                            in.endObject();
                        }
                        default -> throw new JsonDataException("Unexpected field at " + in.getPath() + ": " + nextName);
                    }
                }
                in.endObject();
                // ...
                return init.init();
            }

            @Override
            public void toJson(final @NotNull JsonWriter out, final @Nullable Claim.Type value) throws IOException {
                throw new UnsupportedOperationException("NOT IMPLEMENTED");
            }

        };

    }

    public static final class ClaimTypeInit implements LazyInit<Claim.Type> {

        private @Nullable String id;
        private @Nullable Integer radius;
        private @Nullable ItemStack block;
        private @Nullable ItemStack upgradeButton;
        private @Nullable ItemStack[] upgradeCost;

        @Override
        public Claim.Type init() throws IllegalStateException {
            return new Claim.Type(
                    requireNonNull(id),
                    requireNonNull(radius),
                    requireNonNull(block),
                    requireNonNull(upgradeButton),
                    upgradeCost // Can be null
            );
        }

    }

}

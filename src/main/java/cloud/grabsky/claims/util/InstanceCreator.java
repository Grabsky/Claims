package cloud.grabsky.claims.util;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

public final class InstanceCreator {

    public static <T> JsonAdapter.Factory createFactory(final Class<T> clazz, final Supplier<T> supplier) {
        return new JsonAdapter.Factory() {

            @Override
            public @Nullable JsonAdapter<T> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
                if (clazz.isAssignableFrom(Types.getRawType(type)) == false)
                    return null;
                // ...
                final @Nullable JsonAdapter<T> delegate = moshi.nextAdapter(this, type, annotations);
                // ...
                return new JsonAdapter<T>() {

                    @Override
                    public @Nullable T fromJson(final @NotNull JsonReader reader) throws IOException {
                        return supplier.get();
                    }

                    @Override
                    public void toJson(final @NotNull JsonWriter writer, @Nullable final T value) throws IOException {
                        // Delegating...
                        if (delegate != null)
                            delegate.toJson(writer, value);
                    }

                };
            }

        };
    }

}

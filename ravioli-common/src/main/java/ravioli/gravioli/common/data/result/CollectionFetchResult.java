package ravioli.gravioli.common.data.result;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CollectionFetchResult<K> {
    /**
     * Returns a {@link List} containing all fetched items of type {@code <K>}.
     *
     * @return a List of fetched items
     */
    @NotNull
    List<K> items();

    /**
     * Returns {@code true} if there are more results to fetch immediately following
     * the last item in {@link #items()}; {@code false} otherwise.
     *
     * @return {@code true} if there are more results to fetch; {@code false}
     * otherwise
     */
    boolean hasNext();
}

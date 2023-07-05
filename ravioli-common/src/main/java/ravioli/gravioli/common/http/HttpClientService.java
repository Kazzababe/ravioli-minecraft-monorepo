package ravioli.gravioli.common.http;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HttpClientService {
    /**
     * Make a post request to the given endpoint.
     *
     * @param url                   The URL to hit
     * @param body                  The body of the request
     * @param additionalHeaders     Any additional headers to send alongside the request
     * @return                      A {@link JsonObject} containing the response data.
     */
    @NotNull CompletableFuture<@Nullable JsonObject> post(@NotNull String url, @NotNull JsonObject body,
                                                          @NotNull Map<String, String> additionalHeaders);

    /**
     * Make a get request to the given endpoint.
     *
     * @param url                   The URL to hit
     * @param additionalHeaders     Any additional headers to send alongside the request
     * @return                      A {@link JsonObject} containing the response data
     */
    @NotNull CompletableFuture<@Nullable JsonObject> get(@NotNull String url,
                                                         @NotNull Map<String, String> additionalHeaders);
}

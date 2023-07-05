package ravioli.gravioli.common.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class OkHttpClientService implements HttpClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpClientService.class.getSimpleName());
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;

    public OkHttpClientService() {
        this.httpClient = new OkHttpClient();
    }

    @Override
    public @NotNull CompletableFuture<@Nullable JsonObject> post(@NotNull final String url,
                                                                 @NotNull final JsonObject body,
                                                                 @NotNull final Map<String, String> additionalHeaders) {
        final RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        final Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .headers(Headers.of(additionalHeaders))
            .build();

        return CompletableFuture.supplyAsync(() -> {
            try (final Response response = this.httpClient.newCall(request).execute()) {
                final ResponseBody responseBody = response.body();

                if (responseBody == null) {
                    return null;
                }
                final JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();

                jsonObject.addProperty("statusCode", response.code());

                return jsonObject;
            } catch (final IOException e) {
                LOGGER.error("Error posting request to " + url + ".", e);

                throw new CompletionException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable JsonObject> get(@NotNull final String url,
                                                                @NotNull final Map<String, String> additionalHeaders) {
        final Request request = new Request.Builder()
            .url(url)
            .get()
            .headers(Headers.of(additionalHeaders))
            .build();

        return CompletableFuture.supplyAsync(() -> {
            try (final Response response = this.httpClient.newCall(request).execute()) {
                final ResponseBody responseBody = response.body();

                if (responseBody == null) {
                    return null;
                }
                final JsonObject jsonObject = JsonParser.parseString(responseBody.string()).getAsJsonObject();

                jsonObject.addProperty("statusCode", response.code());

                return jsonObject;
            } catch (final IOException e) {
                LOGGER.error("Error posting request to " + url + ".", e);

                throw new CompletionException(e);
            }
        });
    }
}

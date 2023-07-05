package ravioli.gravioli.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.service.CurrencyService;
import ravioli.gravioli.common.currency.service.RestfulCurrencyService;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.http.OkHttpClientService;
import ravioli.gravioli.core.api.RavioliPlugin;
import ravioli.gravioli.core.resourcepack.font.FontWidth;
import ravioli.gravioli.core.resourcepack.font.FontWidths;
import ravioli.gravioli.core.user.BukkitUser;
import ravioli.gravioli.core.user.listener.UserListener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class RavioliCorePlugin extends RavioliPlugin {
    public RavioliCorePlugin() {
        Platform.setUserClass(BukkitUser.class);
        Platform.setDefaultRedisProvider(this.redisProvider());
        Platform.setDefaultPostgresProvider(this.postgresProvider());
    }

    @Override
    protected void onPluginLoad() {
        this.registerFontWidths();
    }

    @Override
    protected void onPluginEnable() {
        Bukkit.getPluginManager().registerEvents(new UserListener(), this);
    }

    private void registerFontWidths() {
        final File file = this.loadResource("font-widths.json");

        try (final Reader fileReader = new FileReader(file)) {
            final JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();

            for (final String groupKey : jsonObject.keySet()) {
                final JsonObject fontGroupObject = jsonObject.getAsJsonObject(groupKey);
                final JsonArray fontsJsonArray = fontGroupObject.getAsJsonArray("fonts");
                final FontWidth fontWidth = new FontWidth(fontGroupObject);

                for (final JsonElement jsonElement : fontsJsonArray) {
                    final String key = jsonElement.getAsString();

                    FontWidths.registerFontWidth(Key.key(key), fontWidth);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}

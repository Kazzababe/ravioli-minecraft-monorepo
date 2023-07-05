package ravioli.gravioli.common.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RavioliLocale<T> {
    void send(@NotNull T user, @NotNull String key, @NotNull TagResolver... placeholders);

    @NotNull List<Component> get(@NotNull String key, @NotNull TagResolver... placeholders);

    void reload();
}

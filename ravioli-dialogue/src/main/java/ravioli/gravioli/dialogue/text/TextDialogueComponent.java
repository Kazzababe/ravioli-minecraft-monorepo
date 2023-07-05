package ravioli.gravioli.dialogue.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TextDialogueComponent implements DialogueComponent {
    public static @NotNull TextDialogueComponent parse(@NotNull final JsonObject jsonObject) {
        if (!jsonObject.has("text")) {
            throw new RuntimeException("Missing \"text\" property.");
        }
        final JsonElement textObject = jsonObject.get("text");

        if (!textObject.isJsonPrimitive()) {
            throw new RuntimeException("Invalid value for \"text\" property.");
        }
        return new TextDialogueComponent(textObject.getAsString());
    }

    private final String text;

    public TextDialogueComponent(@NotNull final String text) {
        this.text = text;
    }

    @Override
    public @NotNull String getText() {
        return this.text;
    }

    @Override
    public @NotNull Component getFormattedText(@NotNull final Player player) {
        return MiniMessage.miniMessage().deserialize(this.text.replace("%player%", player.getName()));
    }
}

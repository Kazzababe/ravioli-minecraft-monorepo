package ravioli.gravioli.dialogue.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.action.DialogueAction;
import ravioli.gravioli.dialogue.registry.DialogueRegistry;

public class ActionableDialogueComponent implements DialogueComponent {
    public static @NotNull ActionableDialogueComponent parse(@NotNull final JsonObject jsonObject) {
        if (!jsonObject.has("action")) {
            throw new RuntimeException("Missing \"action\" property.");
        }
        final JsonElement jsonElement = jsonObject.get("action");

        if (!jsonElement.isJsonPrimitive()) {
            throw new RuntimeException("Invalid value for \"action\" property.");
        }
        final String actionId = jsonElement.getAsString();
        final DialogueAction action = DialogueRegistry.REGISTRY.parseAction(actionId, jsonObject);

        if (action == null) {
            throw new RuntimeException("Invalid value for \"action\" property.");
        }
        return new ActionableDialogueComponent(action);
    }

    private final DialogueAction action;

    public ActionableDialogueComponent(@NotNull final DialogueAction action) {
        this.action = action;
    }

    @Override
    public boolean skip() {
        return true;
    }

    @Override
    public @NotNull String getText() {
        return "";
    }

    @Override
    public @NotNull Component getFormattedText(@NotNull final Player player) {
        return Component.empty();
    }

    @Override
    public void perform(@NotNull final Player player) {
        this.action.perform(player);
    }
}

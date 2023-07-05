package ravioli.gravioli.dialogue.action.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.action.DialogueAction;

public class CommandDialogueAction implements DialogueAction {
    public static @NotNull CommandDialogueAction parse(@NotNull final JsonObject jsonObject) {
        if (!jsonObject.has("command")) {
            throw new RuntimeException("Missing \"command\" property.");
        }
        final JsonElement jsonElement = jsonObject.get("command");

        if (!jsonElement.isJsonPrimitive()) {
            throw new RuntimeException("Invalid type of property \"command\".");
        }
        return new CommandDialogueAction(jsonElement.getAsString());
    }

    private final String command;

    public CommandDialogueAction(@NotNull final String command) {
        this.command = command;
    }

    @Override
    public void perform(@NotNull final Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.command.replace("%player%", player.getName()));
    }
}

package ravioli.gravioli.dialogue.text;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OptionDialogueComponent implements DialogueComponent {
    public static @NotNull OptionDialogueComponent parse(@NotNull final JsonObject jsonObject) {
        return new OptionDialogueComponent("option oops!", 0);
    }

    private final String text;
    private final int branch;

    public OptionDialogueComponent(@NotNull final String text, final int branch) {
        this.text = text;
        this.branch = branch;
    }

    @Override
    public @NotNull String getText() {
        return this.text;
    }

    @Override
    public @NotNull Component getFormattedText(@NotNull final Player player) {
        return MiniMessage.miniMessage().deserialize(this.text.replace("%player%", player.getName()));
    }

    public int getBranch() {
        return this.branch;
    }
}

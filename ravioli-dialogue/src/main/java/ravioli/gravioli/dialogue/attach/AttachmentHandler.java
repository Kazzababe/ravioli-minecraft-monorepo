package ravioli.gravioli.dialogue.attach;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface AttachmentHandler {
    void registerAttachment(@NotNull ConfigurationSection section);
}

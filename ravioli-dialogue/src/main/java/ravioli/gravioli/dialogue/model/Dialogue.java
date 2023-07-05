package ravioli.gravioli.dialogue.model;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.config.ConversationConfig;
import ravioli.gravioli.dialogue.requirement.DialogueRequirement;

import java.util.List;

public interface Dialogue {
    @NotNull
    String getId();

    @NotNull
    List<@NotNull DialogueRequirement> getRequirements();

    @NotNull
    List<@NotNull ConversationConfig[]> getConversations();
}

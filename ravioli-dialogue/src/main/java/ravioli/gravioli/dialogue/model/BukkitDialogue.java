package ravioli.gravioli.dialogue.model;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.config.ConversationConfig;
import ravioli.gravioli.dialogue.requirement.DialogueRequirement;

import java.util.List;

public class BukkitDialogue implements Dialogue {
    private final String id;
    private final List<DialogueRequirement> requirements;
    private final List<ConversationConfig[]> conversations;

    public BukkitDialogue(@NotNull final String id,
                          @NotNull final List<DialogueRequirement> requirements,
                          @NotNull final ConversationConfig[]... conversations) {
        this.id = id;
        this.requirements = requirements;
        this.conversations = ImmutableList.copyOf(conversations);
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull List<DialogueRequirement> getRequirements() {
        return this.requirements;
    }

    @Override
    public @NotNull List<@NotNull ConversationConfig[]> getConversations() {
        return this.conversations;
    }
}

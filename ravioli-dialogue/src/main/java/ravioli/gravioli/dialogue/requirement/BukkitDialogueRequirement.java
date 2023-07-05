package ravioli.gravioli.dialogue.requirement;

public abstract class BukkitDialogueRequirement implements DialogueRequirement {

    private final int priority;

    public BukkitDialogueRequirement(final int priority) {
        this.priority = priority;
    }

    @Override
    public final int getPriority() {
        return this.priority;
    }
}

package ravioli.gravioli.dialogue.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.command.common.context.CommandContext;
import ravioli.gravioli.core.command.RavioliCommand;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.service.DialogueService;

import java.io.File;

public class DialogueCommand extends RavioliCommand {
    private final File dialogueFolder;
    private final DialogueService dialogueService;

    public DialogueCommand(@NotNull final File dialogueFolder, @NotNull final DialogueService dialogueService) {
        super("dialogue");

        this.dialogueFolder = dialogueFolder;
        this.dialogueService = dialogueService;
    }

    @Override
    protected void init() {
        this.registerSubCommand(
            DEFAULT_COMMAND_META,
            Literal("attach-and-load"),
            String("dialogue").asyncExecutor(this::onLoadAndAttach));
    }

    private void onLoadAndAttach(@NotNull final CommandContext<CommandSender> context) {
        if (!(context.getSource() instanceof final Player player)) {
            return;
        }
        final Entity entityTarget = player.getTargetEntity(20);

        if (!(entityTarget instanceof final LivingEntity target)) {
            player.sendMessage("no target entity");

            return;
        }
        final String dialogueId = context.get("dialogue");

        try {
            File file = new File(this.dialogueFolder, dialogueId);
            final DialogueGroup dialogueGroup = this.dialogueService.loadDialogue(file);

            this.dialogueService.attachDialogue(target, dialogueGroup);

            player.sendMessage("Attached.");
        } catch (final Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}

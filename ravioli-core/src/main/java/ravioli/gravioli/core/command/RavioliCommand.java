package ravioli.gravioli.core.command;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.command.bukkit.command.Command;

public abstract class RavioliCommand extends Command {
    private boolean useHelpCommand = true;

    public RavioliCommand(@NotNull final String root, @NotNull final String... aliases) {
        super(root, aliases);

        this.init();

        if (!this.useHelpCommand) {
            return;
        }
        this.registerSubCommand(
                DEFAULT_COMMAND_META,
                Literal("help")
                        .exceptionally((source, e) -> {

                        })
                        .executor((context) -> {

                        }),
                Integer("page")
                        .withMin(0)
                        .clamp(true)
                        .exceptionally((source, e) -> {

                        })
                        .executor((context) -> {

                        })
        );
    }

    protected final void useHelpCommand(final boolean useHelpCommand) {
        this.useHelpCommand = useHelpCommand;
    }

    protected abstract void init();
}

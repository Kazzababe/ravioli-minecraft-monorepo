package ravioli.gravioli.dialogue.text.animation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.animation.BukkitAnimation;
import ravioli.gravioli.core.animation.timing.AnimationTiming;
import ravioli.gravioli.core.resourcepack.font.FontWidths;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.component.Label;
import ravioli.gravioli.core.resourcepack.layout.component.TexturedPanel;
import ravioli.gravioli.core.resourcepack.layout.element.ResourcePackElements;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;
import ravioli.gravioli.core.resourcepack.overlay.GenericOverlayComponent;
import ravioli.gravioli.core.resourcepack.overlay.actionbar.ActionBarOverlayManager;
import ravioli.gravioli.core.util.SchedulerUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TypingDialogueAnimation extends BukkitAnimation {
    private final String text;
    private final Duration duration;
    private final Player player;

    public TypingDialogueAnimation(@NotNull final String text,
                                   @NotNull final Duration duration,
                                   @NotNull final Player player) {
        super(AnimationTiming.LINEAR);

        this.text = text;
        this.duration = duration;
        this.player = player;
    }

    @Override
    public synchronized void step(final int currentFrame) {
        if (!this.player.isOnline()) {
            this.stop();

            return;
        }
        final String currentText = this.text.substring(0, Math.min(this.text.length(), currentFrame + 1));

        if (currentText.isBlank()) {
            return;
        }
        final List<String> wrappedText = this.wrap(currentText, 300);

        if (currentFrame == this.getTotalFrames() - 1) {
            wrappedText.add("");
            wrappedText.add("[DROP ITEM] to continue...");
        }
        final Panel component = this.createDialogueComponent(wrappedText);
        final ResourcePackComponent overlayComponent = new TexturedPanel.Builder(ResourcePackElements.Generic.DIALOGUE_BOX)
            .alignment(Alignment.CENTER)
            .child(component)
            .create();

        ActionBarOverlayManager.get().showPlayer(player, "DIALOGUE", new GenericOverlayComponent(0, "DIALOGUE", overlayComponent));
    }

    public void exit() {
        this.stop();

        SchedulerUtil.sync().runLater(() -> {
            ActionBarOverlayManager.get().hideFromPlayer(this.player, "DIALOGUE");
        }, 1);
    }

    public void skipToEnd() {
        this.stop();
        this.step(this.getTotalFrames() - 1);
    }

    @Override
    public int getTotalFrames() {
        return this.text.length();
    }

    @Override
    public @NotNull Duration getDuration() {
        return this.duration;
    }

    private @NotNull Panel createDialogueComponent(@NotNull final List<String> wrappedText) {
        final Panel.Builder panelBuilder = new Panel.Builder((int) (ResourcePackElements.Generic.DIALOGUE_BOX.width() * 0.75))
            .alignment(Alignment.CENTER);

        for (int i = 0; i < wrappedText.size(); i++) {
            final String line = wrappedText.get(i);

            panelBuilder.child(
                new Label.Builder(line)
                    .alignment(Alignment.LEFT)
                    .font(Key.key("ravioli", "default/" + -(10 + i * 10)))
                    .contentModifiers((builder) -> builder.color(TextColor.fromHexString("#4504f9")))
                    .create()
            );
        }
        return panelBuilder.create();
    }

    private @NotNull List<String> wrap(@NotNull final String text, final int width) {
        final String[] words = text.split(" ");
        final List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (final String word : words) {
            final int currentWidth = FontWidths.width(currentLine + word);

            if (currentWidth > width) {
                lines.add(currentLine.toString());

                currentLine = new StringBuilder();
            }
            currentLine.append(word)
                .append(" ");
        }
        lines.add(currentLine.toString());

        return lines;

    }
}

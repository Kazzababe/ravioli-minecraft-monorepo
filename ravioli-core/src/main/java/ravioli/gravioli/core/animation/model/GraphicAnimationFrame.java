package ravioli.gravioli.core.animation.model;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record GraphicAnimationFrame(char content, int width, @NotNull Key fontKey) {

}
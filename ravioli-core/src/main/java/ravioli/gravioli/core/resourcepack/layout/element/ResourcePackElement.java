package ravioli.gravioli.core.resourcepack.layout.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element in a Minecraft Resource Pack.
 * <p></p>
 * A Resource Pack element provides a way to map a specific character to an image.
 * This allows custom images to be added into the game via fonts.
 * Each element is associated with a specific font and has a defined width.
 */
public interface ResourcePackElement {

    /**
     * Returns the character mapped to an image in this Resource Pack element.
     * <p></p>
     * The character is used as a unique identifier to map a specific image.
     * When this character is encountered in the game, the associated image will be displayed.
     *
     * @return the character that is associated with an image
     */
    char character();

    /**
     * Returns the width of the image associated with this Resource Pack element.
     * <p></p>
     * The width is used to specify the dimensions of the image in the game.
     * This ensures that the image fits properly within the game's graphical interface.
     *
     * @return the width of the associated image
     */
    int width();

    /**
     * Returns the font that this Resource Pack element is associated with.
     * <p></p>
     * The font is used to group a set of Resource Pack elements.
     * Each font can have multiple characters, each associated with a different image.
     * This allows for the creation of a collection of images that can be displayed in the game as a custom font.
     *
     * @return the {@link Key} representing the font that this element is associated with
     */
    @NotNull
    Key font();
}

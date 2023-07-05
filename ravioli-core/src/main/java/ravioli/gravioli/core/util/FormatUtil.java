package ravioli.gravioli.core.util;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class FormatUtil {
    private static final DecimalFormat COMMA_INTEGER_FORMAT =new DecimalFormat("#,###");

    public static @NotNull String formatCommas(final int number) {
        return COMMA_INTEGER_FORMAT.format(number);
    }
}

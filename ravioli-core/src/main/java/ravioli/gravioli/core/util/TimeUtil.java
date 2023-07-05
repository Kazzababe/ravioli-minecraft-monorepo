package ravioli.gravioli.core.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Period;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for time-based methods and constants.
 */
public final class TimeUtil {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(-*\\d+)([wdhms]{1,2})");
    private static final Pattern PERIOD_PATTERN = Pattern.compile("(-*\\d+)([ymd])");

    private static final Table<ChronoUnit, TextStyle, String> FORMATTED_UNIT = ArrayTable.create(
            List.of(
                    ChronoUnit.YEARS,
                    ChronoUnit.MONTHS,
                    ChronoUnit.WEEKS,
                    ChronoUnit.DAYS,
                    ChronoUnit.HOURS,
                    ChronoUnit.MINUTES,
                    ChronoUnit.SECONDS,
                    ChronoUnit.MILLIS),
            List.of(TextStyle.FULL, TextStyle.SHORT, TextStyle.NARROW));

    static {
        FORMATTED_UNIT.put(ChronoUnit.YEARS, TextStyle.FULL, "year");
        FORMATTED_UNIT.put(ChronoUnit.YEARS, TextStyle.SHORT, "yr");
        FORMATTED_UNIT.put(ChronoUnit.YEARS, TextStyle.NARROW, "y");
        FORMATTED_UNIT.put(ChronoUnit.MONTHS, TextStyle.FULL, "month");
        FORMATTED_UNIT.put(ChronoUnit.MONTHS, TextStyle.SHORT, "mth");
        FORMATTED_UNIT.put(ChronoUnit.MONTHS, TextStyle.NARROW, "mn");
        FORMATTED_UNIT.put(ChronoUnit.WEEKS, TextStyle.FULL, "week");
        FORMATTED_UNIT.put(ChronoUnit.WEEKS, TextStyle.SHORT, "wk");
        FORMATTED_UNIT.put(ChronoUnit.WEEKS, TextStyle.NARROW, "w");
        FORMATTED_UNIT.put(ChronoUnit.DAYS, TextStyle.FULL, "day");
        FORMATTED_UNIT.put(ChronoUnit.DAYS, TextStyle.SHORT, "day");
        FORMATTED_UNIT.put(ChronoUnit.DAYS, TextStyle.NARROW, "d");
        FORMATTED_UNIT.put(ChronoUnit.HOURS, TextStyle.FULL, "hour");
        FORMATTED_UNIT.put(ChronoUnit.HOURS, TextStyle.SHORT, "hr");
        FORMATTED_UNIT.put(ChronoUnit.HOURS, TextStyle.NARROW, "h");
        FORMATTED_UNIT.put(ChronoUnit.MINUTES, TextStyle.FULL, "minute");
        FORMATTED_UNIT.put(ChronoUnit.MINUTES, TextStyle.SHORT, "min");
        FORMATTED_UNIT.put(ChronoUnit.MINUTES, TextStyle.NARROW, "m");
        FORMATTED_UNIT.put(ChronoUnit.SECONDS, TextStyle.FULL, "second");
        FORMATTED_UNIT.put(ChronoUnit.SECONDS, TextStyle.SHORT, "sec");
        FORMATTED_UNIT.put(ChronoUnit.SECONDS, TextStyle.NARROW, "s");
        FORMATTED_UNIT.put(ChronoUnit.MILLIS, TextStyle.FULL, "millisecond");
        FORMATTED_UNIT.put(ChronoUnit.MILLIS, TextStyle.SHORT, "milli");
        FORMATTED_UNIT.put(ChronoUnit.MILLIS, TextStyle.NARROW, "ms");
    }

    private TimeUtil() {}

    /**
     * Parse a {@link Duration} instance from the given input string. The input string is expected
     * to comply with the format {@code (xtime_suffix)+} where {@code x} is a positive or negative
     * integer and {@code time_suffix} is any one of the following suffixes:
     * <table>
     *   <tr>
     *     <th>Suffix (case sensitive)</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>w</td>
     *     <td>Weeks</td>
     *   </tr>
     *   <tr>
     *     <td>d</td>
     *     <td>Days</td>
     *   </tr>
     *   <tr>
     *     <td>h</td>
     *     <td>Hours</td>
     *   </tr>
     *   <tr>
     *     <td>m</td>
     *     <td>Minutes</td>
     *   </tr>
     *   <tr>
     *     <td>s</td>
     *     <td>Seconds</td>
     *   </tr>
     *   <tr>
     *     <td>ms</td>
     *     <td>Milliseconds</td>
     *   </tr>
     * </table>
     * Time units can be strung together with or without whitespace. An example input would be
     * {@code 2w10d6h30m15s500ms} which would result in a Duration of 24 days, 6 hours, 30 minutes
     * 15 seconds, and 500 milliseconds. The order of time units is not at all relevant and will
     * not affect the outcome of the returned duration.
     *
     * @param input the input string
     * @param allowNegative whether or not negative values are allowed
     *
     * @return the parsed duration
     *
     * @throws IllegalArgumentException if the input string is null or blank ({@link String#isBlank()}),
     * does not contain any parseable time, or contains an unsupported time suffix
     * @throws IllegalArgumentException if a negative value is provided and {@code allowNegative} is
     * {@code false}
     */
    @NotNull
    public static Duration parseDuration(@NotNull final String input, final boolean allowNegative) {
        Preconditions.checkArgument(input != null && !input.isBlank(), "input must not be null or blank");

        final Matcher matcher = DURATION_PATTERN.matcher(input.replace(" ", ""));
        long milliseconds = 0;
        boolean negative = false;

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Malformatted input argument: " + input + ". Must comply with " + DURATION_PATTERN.pattern());
        }
        matcher.reset();

        while (matcher.find()) {
            final String suffix = matcher.group(2);
            int amount = NumberUtils.toInt(matcher.group(1));

            negative |= (amount < 0);

            if (negative && !allowNegative) {
                throw new IllegalArgumentException("Negative values are not supported: " + amount + suffix);
            }
            amount = Math.abs(amount);
            milliseconds += switch (suffix) {
                case "w" -> TimeUnit.DAYS.toMillis(amount * 7L);
                case "d" -> TimeUnit.DAYS.toMillis(amount);
                case "h" -> TimeUnit.HOURS.toMillis(amount);
                case "m" -> TimeUnit.MINUTES.toMillis(amount);
                case "s" -> TimeUnit.SECONDS.toMillis(amount);
                case "ms" -> amount;
                default -> throw new IllegalArgumentException("Unsupported time suffix: " + suffix);};
        }
        final Duration duration = Duration.ofMillis(milliseconds);

        return negative ? duration.negated() : duration;
    }

    /**
     * Parse a {@link Duration} instance from the given input string. The input string is expected
     * to comply with the format {@code (xtime_suffix)+} where {@code x} is a positive integer and
     * {@code time_suffix} is any one of the following suffixes:
     * <table>
     *   <tr>
     *     <th>Suffix (case sensitive)</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>w</td>
     *     <td>Weeks</td>
     *   </tr>
     *   <tr>
     *     <td>d</td>
     *     <td>Days</td>
     *   </tr>
     *   <tr>
     *     <td>h</td>
     *     <td>Hours</td>
     *   </tr>
     *   <tr>
     *     <td>m</td>
     *     <td>Minutes</td>
     *   </tr>
     *   <tr>
     *     <td>s</td>
     *     <td>Seconds</td>
     *   </tr>
     *   <tr>
     *     <td>ms</td>
     *     <td>Milliseconds</td>
     *   </tr>
     * </table>
     * Time units can be strung together with or without whitespace. An example input would be
     * {@code 2w10d6h30m15s500ms} which would result in a Duration of 24 days, 6 hours, 30 minutes
     * 15 seconds, and 500 milliseconds. The order of time units is not at all relevant and will
     * not affect the outcome of the returned duration.
     * <p>
     * <strong>NOTE:</strong> Negative durations are not supported. In order to support negative
     * durations, use {@link #parseDuration(String, boolean)}.
     *
     * @param input the input string
     *
     * @return the duration
     *
     * @throws IllegalArgumentException if the input string is null or blank ({@link String#isBlank()}),
     * does not contain any parseable time, or contains an unsupported time suffix
     * @throws IllegalArgumentException if a negative value is provided
     */
    @NotNull
    public static Duration parseDuration(@NotNull final String input) {
        return parseDuration(input, false);
    }

    /**
     * Parse a {@link Period} instance from the given input string. The input string is expected
     * to comply with the format {@code (xtime_suffix)+} where {@code x} is a positive integer and
     * {@code time_suffix} is any one of the following suffixes:
     * <table>
     *   <tr>
     *     <th>Suffix (case sensitive)</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>y</td>
     *     <td>Years</td>
     *   </tr>
     *   <tr>
     *     <td>m</td>
     *     <td>Months</td>
     *   </tr>
     *   <tr>
     *     <td>d</td>
     *     <td>Days</td>
     *   </tr>
     * </table>
     * Time units can be strung together with or without whitespace. An example input would be
     * {@code 2y6m10d} which would result in a Period of 2 years, 6 months, and 10 days. The order
     * of time units is not at all relevant and will not affect the outcome of the returned period.
     * <p>
     * <strong>NOTE:</strong> The returned period <strong>IS NOT NORMALIZED</strong>! If a value of
     * "5y24m" is passed, a Period consisting of 5 years and 24 months will be returned. If it is
     * desired to return a Period that consists instead of 7 years, it is up to the caller to invoke
     * {@link Period#normalized()} on the result of this method.
     *
     * @param input the input string
     * @param allowNegative whether or not negative values are allowed
     *
     * @return the period
     *
     * @throws IllegalArgumentException if the input string is null or blank ({@link String#isBlank()}),
     * does not contain any parseable time, or contains an unsupported time suffix
     * @throws IllegalArgumentException if a negative value is provided
     */
    @NotNull
    public static Period parsePeriod(@NotNull final String input, final boolean allowNegative) {
        Preconditions.checkArgument(input != null && !input.isBlank(), "input must not be null or blank");

        final Matcher matcher = PERIOD_PATTERN.matcher(input.replace(" ", ""));
        int years = 0, months = 0, days = 0;
        boolean negative = false;

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Malformatted input argument: " + input + ". Must comply with " + PERIOD_PATTERN.pattern());
        }
        matcher.reset();

        while (matcher.find()) {
            final String suffix = matcher.group(2);
            int amount = NumberUtils.toInt(matcher.group(1));

            negative |= (amount < 0);

            if (negative && !allowNegative) {
                throw new IllegalArgumentException("Negative values are not supported: " + amount + suffix);
            }
            amount = Math.abs(amount);

            switch (suffix) {
                case "y" -> years = amount;
                case "m" -> months = amount;
                case "d" -> days = amount;
                default -> throw new IllegalArgumentException("Unsupported time suffix: " + suffix);
            }
        }
        final Period period = Period.of(years, months, days);

        return negative ? period.negated() : period;
    }

    /**
     * Parse a {@link Period} instance from the given input string. The input string is expected
     * to comply with the format {@code (xtime_suffix)+} where {@code x} is a positive integer and
     * {@code time_suffix} is any one of the following suffixes:
     * <table>
     *   <tr>
     *     <th>Suffix (case sensitive)</th>
     *     <th>Description</th>
     *   </tr>
     *   <tr>
     *     <td>y</td>
     *     <td>Years</td>
     *   </tr>
     *   <tr>
     *     <td>m</td>
     *     <td>Months</td>
     *   </tr>
     *   <tr>
     *     <td>d</td>
     *     <td>Days</td>
     *   </tr>
     * </table>
     * Time units can be strung together with or without whitespace. An example input would be
     * {@code 2y6m10d} which would result in a Period of 2 years, 6 months, and 10 days. The order
     * of time units is not at all relevant and will not affect the outcome of the returned period.
     * <p>
     * <strong>NOTE:</strong> The returned period <strong>IS NOT NORMALIZED</strong>! If a value of
     * "5y24m" is passed, a Period consisting of 5 years and 24 months will be returned. If it is
     * desired to return a Period that consists instead of 7 years, it is up to the caller to invoke
     * {@link Period#normalized()} on the result of this method.
     * <p>
     * Additionally, negative periods are not supported. In order to support negative periods, use
     * {@link #parsePeriod(String, boolean)}.
     *
     * @param input the input string
     *
     * @return the period
     *
     * @throws IllegalArgumentException if the input string is null or blank ({@link String#isBlank()}),
     * does not contain any parseable time, or contains an unsupported time suffix
     * @throws IllegalArgumentException if a negative value is provided
     */
    @NotNull
    public static Period parsePeriod(@NotNull final String input) {
        return parsePeriod(input, false);
    }

    /**
     * Format the provided {@link Duration} as a human readable string with the given {@link TextStyle}.
     * If the duration is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param duration the duration to format
     * @param style the style with which to format the duration
     * @param now the string to use if the duration {@link Duration#isZero() isZero()}
     *
     * @return the formatted duration string
     */
    @NotNull
    public static String formatDuration(@NotNull final Duration duration, @NotNull final TextStyle style,
                                        @NotNull final String now) {
        Preconditions.checkArgument(duration != null, "duration must not be null");
        Preconditions.checkArgument(style != null, "style must not be null");
        Preconditions.checkArgument(now != null, "now must not be null");

        final TextStyle currentStyle = style.asNormal(); // Make sure the style is normal. We don't care about standalone
        final boolean past = duration.isNegative();
        final Duration currentDuration = duration.abs(); // We only care about absolute time
        final StringBuilder string = new StringBuilder();
        final boolean pretty = currentStyle != TextStyle.NARROW;

        append(string, currentDuration.toDaysPart(), FORMATTED_UNIT.get(ChronoUnit.DAYS, currentStyle), pretty);
        append(string, currentDuration.toHoursPart(), FORMATTED_UNIT.get(ChronoUnit.HOURS, currentStyle), pretty);
        append(string, currentDuration.toMinutesPart(), FORMATTED_UNIT.get(ChronoUnit.MINUTES, currentStyle), pretty);
        append(string, currentDuration.toSecondsPart(), FORMATTED_UNIT.get(ChronoUnit.SECONDS, currentStyle), pretty);
        append(string, currentDuration.toMillisPart(), FORMATTED_UNIT.get(ChronoUnit.MILLIS, currentStyle), pretty);

        if (string.isEmpty()) {
            return now;
        }
        final String text = string.toString().trim();

        return past ? (text + " ago") : text;
    }

    /**
     * Format the provided {@link Duration} as a human readable string with the given {@link TextStyle}.
     * If the duration is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param duration the duration to format
     * @param style the style with which to format the duration
     *
     * @return the formatted duration string
     */
    @NotNull
    public static String formatDuration(@NotNull final Duration duration, @NotNull final TextStyle style) {
        return formatDuration(duration, style, "now");
    }

    /**
     * Format the provided {@link Duration} as a human readable string with {@link TextStyle#FULL}.
     * If the duration is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param duration the duration to format
     * @param now the string to use if the duration {@link Duration#isZero() isZero()}
     *
     * @return the formatted duration string
     */
    @NotNull
    public static String formatDuration(@NotNull final Duration duration, @NotNull final String now) {
        return formatDuration(duration, TextStyle.FULL, now);
    }

    /**
     * Format the provided {@link Duration} as a human readable string with {@link TextStyle#FULL}.
     * If the duration is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param duration the duration to format
     *
     * @return the formatted duration string
     */
    @NotNull
    public static String formatDuration(@NotNull final Duration duration) {
        return formatDuration(duration, TextStyle.FULL);
    }

    /**
     * Format the provided {@link Period} as a human readable string with the given {@link TextStyle}.
     * If the period is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param period the period to format
     * @param style the style with which to format the period
     *
     * @return the formatted period string
     */
    public static String formatPeriod(@NotNull final Period period, @NotNull final TextStyle style) {
        Preconditions.checkArgument(period != null, "period must not be null");
        Preconditions.checkArgument(style != null, "style must not be null");

        final TextStyle currentStyle = style.asNormal(); // Make sure the style is normal. We don't care about standalone
        final boolean past = period.isNegative();
        Period currentPeriod = period.normalized();

        if (past) {
            currentPeriod = period.negated(); // Period#abs() when?
        }
        final StringBuilder string = new StringBuilder();
        final boolean pretty = currentStyle != TextStyle.NARROW;

        append(string, currentPeriod.getYears(), FORMATTED_UNIT.get(ChronoUnit.YEARS, currentStyle), pretty);
        append(string, currentPeriod.getMonths(), FORMATTED_UNIT.get(ChronoUnit.MONTHS, currentStyle), pretty);
        append(string, currentPeriod.getDays(), FORMATTED_UNIT.get(ChronoUnit.DAYS, currentStyle), pretty);

        if (string.isEmpty()) {
            return "0 days";
        }
        final String text = string.toString().trim();

        return past ? (text + " ago") : text;
    }

    /**
     * Format the provided {@link Period} as a human readable string with {@link TextStyle#FULL}.
     * If the period is negative, {@code " ago"} will be appended to the end of the string.
     *
     * @param period the period to format
     *
     * @return the formatted period string
     */
    public static String formatPeriod(@NotNull final Period period) {
        return formatPeriod(period, TextStyle.FULL);
    }

    private static void append(@NotNull final StringBuilder builder, final long quantity, @NotNull final String suffix,
                               final boolean pretty) {
        if (quantity <= 0) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(pretty ? ", " : " ");
        }
        builder.append(quantity);

        if (pretty) {
            builder.append(" ");
        }
        builder.append(suffix);

        if (quantity > 1 && pretty) {
            builder.append("s");
        }
    }
}
